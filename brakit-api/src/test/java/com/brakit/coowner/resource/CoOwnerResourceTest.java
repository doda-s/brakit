package com.brakit.coowner.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

import com.brakit.support.TestUsers;
import com.brakit.support.TestUsers.TestUser;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
class CoOwnerResourceTest {

	private static final String CO_OWNERS = "/api/tournaments/{id}/co-owners";
	private static final String CO_OWNER = CO_OWNERS + "/{userId}";

	private static TestUser creator;

	// One creator per test class; its token is reused for every creator request
	private static RequestSpecification asCreator() {
		if (creator == null) {
			creator = TestUsers.newUser();
		}
		return given().auth().oauth2(creator.token());
	}

	private static RequestSpecification as(TestUser user) {
		return given().auth().oauth2(user.token());
	}

	private static long userId(TestUser user) {
		return as(user).get("/api/auth/me").then().statusCode(200).extract().jsonPath().getLong("id");
	}

	private long createTournament(String visibility) {
		return asCreator()
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

	// Creates a fresh user and makes them a co-owner of the tournament
	private TestUser addCoOwner(long tournamentId) {
		var coOwner = TestUsers.newUser();
		asCreator()
			.contentType(ContentType.JSON)
			.body("{\"email\": \"%s\"}".formatted(coOwner.email()))
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(201);
		return coOwner;
	}

	@Test
	void creatorAddsCoOwnerByEmailIgnoringCase() {
		long tournamentId = createTournament("PUBLIC");
		var coOwner = TestUsers.newUser();
		long coOwnerId = userId(coOwner);

		asCreator()
			.contentType(ContentType.JSON)
			.body("{\"email\": \"%s\"}".formatted(coOwner.email().toUpperCase()))
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(201)
			.body("id", equalTo((int) coOwnerId))
			.body("email", equalTo(coOwner.email()))
			.body("name", notNullValue());

		asCreator()
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(200).body("id", contains((int) coOwnerId));

		// Co-owners see the list too
		as(coOwner)
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(200).body("email", contains(coOwner.email()));
	}

	@Test
	void listIsEmptyForTournamentWithoutCoOwners() {
		long tournamentId = createTournament("PUBLIC");

		asCreator()
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(200).body("$", empty());
	}

	@Test
	void rejectsEmailWithoutUser() {
		long tournamentId = createTournament("PUBLIC");

		asCreator()
			.contentType(ContentType.JSON)
			.body("{\"email\": \"ninguem@brakit.com\"}")
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(404)
			.body("message", equalTo("No user with email ninguem@brakit.com was found."));
	}

	@Test
	void rejectsInvalidEmail() {
		long tournamentId = createTournament("PUBLIC");

		asCreator()
			.contentType(ContentType.JSON)
			.body("{\"email\": \"not-an-email\"}")
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(400)
			.body("message", containsString("email"));

		asCreator()
			.contentType(ContentType.JSON)
			.body("{}")
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(400)
			.body("message", containsString("email"));
	}

	@Test
	void rejectsExistingCoOwnerAndCreator() {
		long tournamentId = createTournament("PUBLIC");
		var coOwner = addCoOwner(tournamentId);

		asCreator()
			.contentType(ContentType.JSON)
			.body("{\"email\": \"%s\"}".formatted(coOwner.email()))
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(409)
			.body("message", equalTo("User %s is already an owner of this tournament.".formatted(coOwner.email())));

		asCreator()
			.contentType(ContentType.JSON)
			.body("{\"email\": \"%s\"}".formatted(creator.email()))
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(409);

		asCreator()
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(200).body("email", contains(coOwner.email()));
	}

	@Test
	void coOwnerManagesTournamentTeamsAndMembers() {
		long tournamentId = createTournament("PUBLIC");
		var coOwner = addCoOwner(tournamentId);

		as(coOwner)
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Copa do Co-dono\"}")
		.when()
			.put("/api/tournaments/{id}", tournamentId)
		.then()
			.statusCode(200)
			.body("name", equalTo("Copa do Co-dono"))
			.body("viewerRole", equalTo("CO_OWNER"));

		long teamId = as(coOwner)
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Alfa\"}")
		.when()
			.post("/api/tournaments/{id}/teams", tournamentId)
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");

		as(coOwner)
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Beta\"}")
		.when()
			.put("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
		.then()
			.statusCode(200)
			.body("name", equalTo("Beta"));

		var members = "/api/tournaments/{id}/teams/{teamId}/members";
		long memberId = as(coOwner)
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Fulano\"}")
		.when()
			.post(members, tournamentId, teamId)
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");

		as(coOwner)
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Ciclano\"}")
		.when()
			.put(members + "/{memberId}", tournamentId, teamId, memberId)
		.then()
			.statusCode(200)
			.body("name", equalTo("Ciclano"));

		as(coOwner)
			.when().delete(members + "/{memberId}", tournamentId, teamId, memberId)
			.then().statusCode(204);

		as(coOwner)
			.when().delete("/api/tournaments/{id}/teams/{teamId}", tournamentId, teamId)
			.then().statusCode(204);
	}

	@Test
	void coOwnerCannotDeleteTournamentNorManageCoOwners() {
		long tournamentId = createTournament("PUBLIC");
		var coOwner = addCoOwner(tournamentId);
		long coOwnerId = userId(coOwner);
		var stranger = TestUsers.newUser();

		as(coOwner)
			.when().delete("/api/tournaments/{id}", tournamentId)
			.then()
			.statusCode(403)
			.body("message", equalTo("Only the creator can delete tournament %d or manage its co-owners.".formatted(tournamentId)));

		as(coOwner)
			.contentType(ContentType.JSON)
			.body("{\"email\": \"%s\"}".formatted(stranger.email()))
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(403);

		// Not even to leave the tournament
		as(coOwner)
			.when().delete(CO_OWNER, tournamentId, coOwnerId)
			.then().statusCode(403);

		asCreator()
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(200).body("id", contains((int) coOwnerId));
		given()
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(200);
	}

	@Test
	void strangerCannotSeeOrManageCoOwnersOfPublicTournament() {
		long tournamentId = createTournament("PUBLIC");
		var coOwner = addCoOwner(tournamentId);
		long coOwnerId = userId(coOwner);
		var stranger = TestUsers.newUser();

		as(stranger)
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(403).body("message", equalTo("Only the owners can modify tournament %d.".formatted(tournamentId)));

		as(stranger)
			.contentType(ContentType.JSON)
			.body("{\"email\": \"%s\"}".formatted(stranger.email()))
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(403);

		as(stranger)
			.when().delete(CO_OWNER, tournamentId, coOwnerId)
			.then().statusCode(403);

		given()
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(401);
	}

	@Test
	void privateTournamentIsVisibleToCoOwnerOnly() {
		long tournamentId = createTournament("PRIVATE");
		var coOwner = addCoOwner(tournamentId);
		var stranger = TestUsers.newUser();

		as(coOwner)
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(200).body("viewerRole", equalTo("CO_OWNER"));

		as(coOwner)
			.when().get("/api/tournaments/{id}/teams", tournamentId)
			.then().statusCode(200);

		// The list is ordered by id, so the newest tournament sits on the last page
		int total = as(coOwner).get("/api/tournaments").then().extract().jsonPath().getInt("totalElements");
		as(coOwner)
			.queryParam("page", total - 1)
			.queryParam("size", 1)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("content.id", contains((int) tournamentId))
			.body("content[0].viewerRole", equalTo("CO_OWNER"));

		as(stranger)
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(404);

		as(stranger)
			.queryParam("size", 100)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("content.id", not(hasItem((int) tournamentId)));

		// Strangers can't tell the private tournament exists through the co-owner endpoints either
		as(stranger)
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(404);

		as(stranger)
			.contentType(ContentType.JSON)
			.body("{\"email\": \"%s\"}".formatted(stranger.email()))
		.when()
			.post(CO_OWNERS, tournamentId)
		.then()
			.statusCode(404);
	}

	@Test
	void viewerRoleReflectsRequester() {
		long tournamentId = createTournament("PUBLIC");
		var coOwner = addCoOwner(tournamentId);
		var stranger = TestUsers.newUser();

		asCreator()
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(200).body("viewerRole", equalTo("CREATOR"));

		as(coOwner)
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(200).body("viewerRole", equalTo("CO_OWNER"));

		as(stranger)
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(200).body("viewerRole", nullValue());

		given()
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(200).body("viewerRole", nullValue());
	}

	@Test
	void removingCoOwnerRevokesAccess() {
		long tournamentId = createTournament("PRIVATE");
		var coOwner = addCoOwner(tournamentId);
		var other = addCoOwner(tournamentId);
		long coOwnerId = userId(coOwner);

		asCreator()
			.when().delete(CO_OWNER, tournamentId, coOwnerId)
			.then().statusCode(204);

		asCreator()
			.when().get(CO_OWNERS, tournamentId)
			.then().statusCode(200).body("email", contains(other.email()));

		as(coOwner)
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(404);

		as(coOwner)
			.contentType(ContentType.JSON)
			.body("{\"name\": \"x\"}")
		.when()
			.put("/api/tournaments/{id}", tournamentId)
		.then()
			.statusCode(404);

		asCreator()
			.when().delete(CO_OWNER, tournamentId, coOwnerId)
			.then()
			.statusCode(404)
			.body("message", equalTo("User %d is not a co-owner of this tournament.".formatted(coOwnerId)));
	}

	@Test
	void unknownTournamentIsNotFound() {
		asCreator()
			.when().get(CO_OWNERS, 999999)
			.then().statusCode(404);
	}

	@Test
	void creatorDeletesTournamentWithCoOwners() {
		long tournamentId = createTournament("PUBLIC");
		var coOwner = addCoOwner(tournamentId);

		asCreator()
			.when().delete("/api/tournaments/{id}", tournamentId)
			.then().statusCode(204);

		as(coOwner)
			.when().get("/api/tournaments/{id}", tournamentId)
			.then().statusCode(404);
	}
}
