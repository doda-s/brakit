package com.brakit.team.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.brakit.support.TestUsers;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
class TeamResourceTest {

	private static String ownerToken;

	// One owner per test class; its token is reused for every write request
	private static RequestSpecification authenticated() {
		if (ownerToken == null) {
			ownerToken = TestUsers.newToken();
		}
		return given().auth().oauth2(ownerToken);
	}

	private long createTournament(String visibility, int teamCountLimit) {
		return authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "Copa", "description": "d", "visibility": "%s", "teamCountLimit": %d}
				""".formatted(visibility, teamCountLimit))
		.when()
			.post("/api/tournaments")
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");
	}

	private long createTournament() {
		return createTournament("PUBLIC", 8);
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

	@Test
	void createsTeamWithTrimmedName() {
		long tournamentId = createTournament();

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"  Os Brabos  \"}")
		.when()
			.post("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.statusCode(201)
			.body("name", equalTo("Os Brabos"))
			.body("tournamentId", equalTo((int) tournamentId));
	}

	@Test
	void rejectsBlankName() {
		long tournamentId = createTournament();

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"  \"}")
		.when()
			.post("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.statusCode(400)
			.body("message", containsString("name"));
	}

	@Test
	void listsAndGetsTeamsPublicly() {
		long tournamentId = createTournament();
		long first = createTeam(tournamentId, "Alfa");
		long second = createTeam(tournamentId, "Beta");

		given()
		.when()
			.get("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.statusCode(200)
			.body("id", contains((int) first, (int) second))
			.body("name", contains("Alfa", "Beta"));

		given()
		.when()
			.get("/api/tournaments/{id}/teams/{teamId}", tournamentId, second)
		.then()
			.statusCode(200)
			.body("name", equalTo("Beta"));
	}

	@Test
	void listIsEmptyForTournamentWithoutTeams() {
		long tournamentId = createTournament();

		given()
		.when()
			.get("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.statusCode(200)
			.body("$", empty());
	}

	@Test
	void rejectsDuplicateNameIgnoringCase() {
		long tournamentId = createTournament();
		createTeam(tournamentId, "Alfa");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \" ALFA \"}")
		.when()
			.post("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.statusCode(409)
			.body("message", equalTo("A team named ALFA already exists in this tournament."));
	}

	@Test
	void allowsSameNameInDifferentTournaments() {
		createTeam(createTournament(), "Alfa");
		createTeam(createTournament(), "Alfa");
	}

	@Test
	void renamesTeam() {
		long tournamentId = createTournament();
		long teamId = createTeam(tournamentId, "Alfa");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Ômega\"}")
		.when()
			.put("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(200)
			.body("id", equalTo((int) teamId))
			.body("name", equalTo("Ômega"));
	}

	@Test
	void renameMayChangeOnlyTheCaseOfItsOwnName() {
		long tournamentId = createTournament();
		long teamId = createTeam(tournamentId, "alfa");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Alfa\"}")
		.when()
			.put("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(200)
			.body("name", equalTo("Alfa"));
	}

	@Test
	void rejectsRenameToAnotherTeamsName() {
		long tournamentId = createTournament();
		createTeam(tournamentId, "Alfa");
		long teamId = createTeam(tournamentId, "Beta");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"alfa\"}")
		.when()
			.put("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(409);
	}

	@Test
	void deletesTeam() {
		long tournamentId = createTournament();
		long teamId = createTeam(tournamentId, "Alfa");

		authenticated()
		.when()
			.delete("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(204);

		given()
		.when()
			.get("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(404)
			.body("message", equalTo("No team with id %d was found.".formatted(teamId)));

		// The name is free again
		createTeam(tournamentId, "Alfa");
	}

	@Test
	void teamFromAnotherTournamentIsNotFound() {
		long tournamentId = createTournament();
		long otherTournamentId = createTournament();
		long teamId = createTeam(otherTournamentId, "Alfa");

		given()
		.when()
			.get("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(404);

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"x\"}")
		.when()
			.put("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(404);

		authenticated()
		.when()
			.delete("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(404);
	}

	@Test
	void returnsNotFoundForUnknownTournament() {
		given()
		.when()
			.get("/api/tournaments/{id}/teams", 999999)
		.then()
			.statusCode(404)
			.body("message", equalTo("No tournament with id 999999 was found."));

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"x\"}")
		.when()
			.post("/api/tournaments/{id}/teams", 999999)
		.then()
			.statusCode(404);
	}

	@Test
	void rejectsTeamsBeyondTournamentLimit() {
		long tournamentId = createTournament("PUBLIC", 2);
		createTeam(tournamentId, "Alfa");
		createTeam(tournamentId, "Beta");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Gama\"}")
		.when()
			.post("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.statusCode(409)
			.body("message", equalTo("Tournament already has the maximum of 2 teams."));
	}

	@Test
	void concurrentCreationsNeverExceedTournamentLimit() {
		int limit = 2;
		int attempts = 8;
		long tournamentId = createTournament("PUBLIC", limit);

		var futures = IntStream.range(0, attempts)
			.mapToObj(i -> CompletableFuture.supplyAsync(() -> authenticated()
				.contentType(ContentType.JSON)
				.body("{\"name\": \"Time %d\"}".formatted(i))
			.when()
				.post("/api/tournaments/{id}/teams", tournamentId)
			.then()
				.extract().statusCode()))
			.toList();
		List<Integer> statuses = futures.stream().map(CompletableFuture::join).toList();

		assertThat(statuses, everyItem(is(oneOf(201, 409))));
		assertThat(statuses.stream().filter(s -> s == 201).count(), equalTo((long) limit));
		given()
		.when()
			.get("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.body("size()", equalTo(limit));
	}

	@Test
	void cannotLowerTeamCountLimitBelowRegisteredTeams() {
		long tournamentId = createTournament("PUBLIC", 4);
		createTeam(tournamentId, "Alfa");
		createTeam(tournamentId, "Beta");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"teamCountLimit\": 1}")
		.when()
			.put("/api/tournaments/{id}", tournamentId)
		.then()
			.statusCode(409)
			.body("message", equalTo("Team count limit 1 is below the 2 teams already registered."));

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"teamCountLimit\": 2}")
		.when()
			.put("/api/tournaments/{id}", tournamentId)
		.then()
			.statusCode(200)
			.body("teamCountLimit", equalTo(2));
	}

	@Test
	void deletingTournamentDeletesItsTeams() {
		long tournamentId = createTournament();
		createTeam(tournamentId, "Alfa");

		authenticated()
		.when()
			.delete("/api/tournaments/{id}", tournamentId)
		.then()
			.statusCode(204);
	}

	@Test
	void writesRequireToken() {
		long tournamentId = createTournament();
		long teamId = createTeam(tournamentId, "Alfa");

		given().contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().post("/api/tournaments/{id}/teams", tournamentId)
			.then().statusCode(401);

		given().contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().put("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
			.then().statusCode(401);

		given()
			.when().delete("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
			.then().statusCode(401);
	}

	@Test
	void onlyOwnerCanManageTeamsOfPublicTournament() {
		long tournamentId = createTournament();
		long teamId = createTeam(tournamentId, "Alfa");
		var otherToken = TestUsers.newToken();

		given().auth().oauth2(otherToken).contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().post("/api/tournaments/{id}/teams", tournamentId)
			.then().statusCode(403);

		given().auth().oauth2(otherToken).contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().put("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
			.then().statusCode(403);

		given().auth().oauth2(otherToken)
			.when().delete("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
			.then().statusCode(403);

		given().auth().oauth2(otherToken)
			.when().get("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
			.then().statusCode(200).body("name", equalTo("Alfa"));
	}

	@Test
	void teamsOfPrivateTournamentAreHiddenFromEveryoneButOwner() {
		long tournamentId = createTournament("PRIVATE", 8);
		long teamId = createTeam(tournamentId, "Alfa");
		var otherToken = TestUsers.newToken();

		authenticated()
			.when().get("/api/tournaments/{id}/teams", tournamentId)
			.then().statusCode(200).body("name", contains("Alfa"));

		given()
			.when().get("/api/tournaments/{id}/teams", tournamentId)
			.then().statusCode(404);

		given().auth().oauth2(otherToken)
			.when().get("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
			.then().statusCode(404);

		given().auth().oauth2(otherToken).contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().post("/api/tournaments/{id}/teams", tournamentId)
			.then().statusCode(404);

		given().auth().oauth2(otherToken)
			.when().delete("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
			.then().statusCode(404);
	}
}
