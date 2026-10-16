package com.brakit.member.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

import com.brakit.support.TestUsers;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
class MemberResourceTest {

	private static final String MEMBERS = "/api/tournaments/{id}/teams/{teamId}/members";
	private static final String MEMBER = MEMBERS + "/{memberId}";

	private static String ownerToken;

	// One owner per test class; its token is reused for every write request
	private static RequestSpecification authenticated() {
		if (ownerToken == null) {
			ownerToken = TestUsers.newToken();
		}
		return given().auth().oauth2(ownerToken);
	}

	private long createTournament(String visibility) {
		return authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "Copa", "description": "d", "visibility": "%s", "teamCountLimit": 8}
				""".formatted(visibility))
		.when()
			.post("/api/tournaments")
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");
	}

	private long createTeam(long tournamentId, String name) {
		return authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"%s\"}".formatted(name))
		.when()
			.post("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");
	}

	private long createMember(long tournamentId, long teamId, String body) {
		return authenticated()
			.contentType(ContentType.JSON)
			.body(body)
		.when()
			.post(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");
	}

	@Test
	void createsMemberWithTrimmedFields() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"  Fulano  \", \"role\": \"  Capitão, joga de atacante  \"}")
		.when()
			.post(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(201)
			.body("teamId", equalTo((int) teamId))
			.body("name", equalTo("Fulano"))
			.body("role", equalTo("Capitão, joga de atacante"));
	}

	@Test
	void roleIsOptional() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Fulano\"}")
		.when()
			.post(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(201)
			.body("role", nullValue());
	}

	@Test
	void rejectsBlankNameAndBlankRole() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \" \", \"role\": \"  \"}")
		.when()
			.post(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(400)
			// Only field paths are asserted: message texts follow the JVM locale
			.body("message", allOf(containsString("name: "), containsString("; role: ")));
	}

	@Test
	void allowsMembersWithSameName() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");
		createMember(tournamentId, teamId, "{\"name\": \"Fulano\"}");
		createMember(tournamentId, teamId, "{\"name\": \"Fulano\"}");
	}

	@Test
	void listsAndGetsMembersPublicly() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");
		long first = createMember(tournamentId, teamId, "{\"name\": \"Fulano\", \"role\": \"Capitão\"}");
		long second = createMember(tournamentId, teamId, "{\"name\": \"Ciclano\"}");

		given()
		.when()
			.get(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(200)
			.body("id", contains((int) first, (int) second))
			.body("name", contains("Fulano", "Ciclano"));

		given()
		.when()
			.get(MEMBER, tournamentId, teamId, first)
		.then()
			.statusCode(200)
			.body("role", equalTo("Capitão"));
	}

	@Test
	void listIsEmptyForTeamWithoutMembers() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");

		given()
		.when()
			.get(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(200)
			.body("$", empty());
	}

	@Test
	void updateReplacesNameAndRole() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");
		long memberId = createMember(tournamentId, teamId, "{\"name\": \"Fulano\", \"role\": \"Capitão\"}");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Beltrano\", \"role\": \"Reserva\"}")
		.when()
			.put(MEMBER, tournamentId, teamId, memberId)
		.then()
			.statusCode(200)
			.body("id", equalTo((int) memberId))
			.body("name", equalTo("Beltrano"))
			.body("role", equalTo("Reserva"));

		// Omitting the role clears it
		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Beltrano\"}")
		.when()
			.put(MEMBER, tournamentId, teamId, memberId)
		.then()
			.statusCode(200)
			.body("role", nullValue());
	}

	@Test
	void deletesMember() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");
		long memberId = createMember(tournamentId, teamId, "{\"name\": \"Fulano\"}");

		authenticated()
		.when()
			.delete(MEMBER, tournamentId, teamId, memberId)
		.then()
			.statusCode(204);

		given()
		.when()
			.get(MEMBER, tournamentId, teamId, memberId)
		.then()
			.statusCode(404)
			.body("message", equalTo("No member with id %d was found.".formatted(memberId)));
	}

	@Test
	void deletingTeamDeletesItsMembers() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");
		createMember(tournamentId, teamId, "{\"name\": \"Fulano\"}");

		authenticated()
		.when()
			.delete("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(204);

		given()
		.when()
			.get(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(404)
			.body("message", equalTo("No team with id %d was found.".formatted(teamId)));
	}

	@Test
	void memberFromAnotherTeamIsNotFound() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");
		long otherTeamId = createTeam(tournamentId, "Beta");
		long memberId = createMember(tournamentId, otherTeamId, "{\"name\": \"Fulano\"}");

		given()
		.when()
			.get(MEMBER, tournamentId, teamId, memberId)
		.then()
			.statusCode(404);

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"x\"}")
		.when()
			.put(MEMBER, tournamentId, teamId, memberId)
		.then()
			.statusCode(404);

		authenticated()
		.when()
			.delete(MEMBER, tournamentId, teamId, memberId)
		.then()
			.statusCode(404);
	}

	@Test
	void teamFromAnotherTournamentIsNotFound() {
		long tournamentId = createTournament("PUBLIC");
		long otherTournamentId = createTournament("PUBLIC");
		long teamId = createTeam(otherTournamentId, "Alfa");

		given()
		.when()
			.get(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(404)
			.body("message", containsString("No team with id"));

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"x\"}")
		.when()
			.post(MEMBERS, tournamentId, teamId)
		.then()
			.statusCode(404);
	}

	@Test
	void writesRequireToken() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");
		long memberId = createMember(tournamentId, teamId, "{\"name\": \"Fulano\"}");

		given().contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().post(MEMBERS, tournamentId, teamId)
			.then().statusCode(401);

		given().contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().put(MEMBER, tournamentId, teamId, memberId)
			.then().statusCode(401);

		given()
			.when().delete(MEMBER, tournamentId, teamId, memberId)
			.then().statusCode(401);
	}

	@Test
	void onlyOwnerCanManageMembersOfPublicTournament() {
		long tournamentId = createTournament("PUBLIC");
		long teamId = createTeam(tournamentId, "Alfa");
		long memberId = createMember(tournamentId, teamId, "{\"name\": \"Fulano\"}");
		var otherToken = TestUsers.newToken();

		given().auth().oauth2(otherToken).contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().post(MEMBERS, tournamentId, teamId)
			.then().statusCode(403);

		given().auth().oauth2(otherToken).contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().put(MEMBER, tournamentId, teamId, memberId)
			.then().statusCode(403);

		given().auth().oauth2(otherToken)
			.when().delete(MEMBER, tournamentId, teamId, memberId)
			.then().statusCode(403);

		given().auth().oauth2(otherToken)
			.when().get(MEMBER, tournamentId, teamId, memberId)
			.then().statusCode(200).body("name", equalTo("Fulano"));
	}

	@Test
	void membersOfPrivateTournamentAreHiddenFromEveryoneButOwner() {
		long tournamentId = createTournament("PRIVATE");
		long teamId = createTeam(tournamentId, "Alfa");
		long memberId = createMember(tournamentId, teamId, "{\"name\": \"Fulano\"}");
		var otherToken = TestUsers.newToken();

		authenticated()
			.when().get(MEMBERS, tournamentId, teamId)
			.then().statusCode(200).body("name", contains("Fulano"));

		given()
			.when().get(MEMBERS, tournamentId, teamId)
			.then().statusCode(404);

		given().auth().oauth2(otherToken)
			.when().get(MEMBER, tournamentId, teamId, memberId)
			.then().statusCode(404);

		given().auth().oauth2(otherToken).contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().post(MEMBERS, tournamentId, teamId)
			.then().statusCode(404);
	}
}
