package com.brakit.tournament.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import com.brakit.support.TestUsers;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
class TournamentResourceTest {

	private static final String VALID_BODY = """
		{"name": "Copa", "description": "Torneio de teste", "visibility": "PUBLIC", "teamCountLimit": 8}
		""";

	private static String ownerToken;

	// One owner per test class; its token is reused for every write request
	private static RequestSpecification authenticated() {
		if (ownerToken == null) {
			ownerToken = TestUsers.newToken();
		}
		return given().auth().oauth2(ownerToken);
	}

	private long createPrivateTournament() {
		return authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "Copa Privada", "description": "d", "visibility": "PRIVATE", "teamCountLimit": 8}
				""")
		.when()
			.post("/api/tournaments")
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");
	}

	private long createTournament() {
		return authenticated()
			.contentType(ContentType.JSON)
			.body(VALID_BODY)
		.when()
			.post("/api/tournaments")
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");
	}

	@Test
	void createsTournament() {
		authenticated()
			.contentType(ContentType.JSON)
			.body(VALID_BODY)
		.when()
			.post("/api/tournaments")
		.then()
			.statusCode(201)
			.body("id", notNullValue())
			.body("name", equalTo("Copa"))
			.body("visibility", equalTo("PUBLIC"))
			.body("teamCountLimit", equalTo(8));
	}

	@Test
	void rejectsInvalidCreateRequest() {
		authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": " ", "description": "d", "visibility": "PRIVATE"}
				""")
		.when()
			.post("/api/tournaments")
		.then()
			.statusCode(400)
			.body("status", equalTo(400))
			.body("message", containsString("name"))
			.body("message", containsString("teamCountLimit"));
	}

	@Test
	void updatesOnlyProvidedFields() {
		long id = createTournament();

		authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "Copa Renomeada", "visibility": "PRIVATE"}
				""")
		.when()
			.put("/api/tournaments/{id}", id)
		.then()
			.statusCode(200)
			.body("id", equalTo((int) id))
			.body("name", equalTo("Copa Renomeada"))
			.body("description", equalTo("Torneio de teste"))
			.body("visibility", equalTo("PRIVATE"))
			.body("teamCountLimit", equalTo(8));
	}

	@Test
	void updateUsesIdFromPathAndIgnoresBodyId() {
		long target = createTournament();
		long other = createTournament();

		authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"id": %d, "name": "Alvo"}
				""".formatted(other))
		.when()
			.put("/api/tournaments/{id}", target)
		.then()
			.statusCode(200)
			.body("id", equalTo((int) target))
			.body("name", equalTo("Alvo"));
	}

	@Test
	void rejectsInvalidUpdateValues() {
		long id = createTournament();

		authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "", "teamCountLimit": -3}
				""")
		.when()
			.put("/api/tournaments/{id}", id)
		.then()
			.statusCode(400)
			.body("message", containsString("name"))
			.body("message", containsString("teamCountLimit"));
	}

	@Test
	void returnsNotFoundForUnknownTournament() {
		authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "x"}
				""")
		.when()
			.put("/api/tournaments/{id}", 999999)
		.then()
			.statusCode(404)
			.body("status", equalTo(404))
			.body("message", equalTo("No tournament with id 999999 was found."));
	}

	@Test
	void deletesTournament() {
		long id = createTournament();

		authenticated()
		.when()
			.delete("/api/tournaments/{id}", id)
		.then()
			.statusCode(204);

		authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "x"}
				""")
		.when()
			.put("/api/tournaments/{id}", id)
		.then()
			.statusCode(404);
	}

	@Test
	void deleteReturnsNotFoundForUnknownTournament() {
		authenticated()
		.when()
			.delete("/api/tournaments/{id}", 999999)
		.then()
			.statusCode(404)
			.body("status", equalTo(404))
			.body("message", equalTo("No tournament with id 999999 was found."));
	}

	@Test
	void getsTournamentById() {
		long id = createTournament();

		given()
		.when()
			.get("/api/tournaments/{id}", id)
		.then()
			.statusCode(200)
			.body("id", equalTo((int) id))
			.body("name", equalTo("Copa"))
			.body("description", equalTo("Torneio de teste"))
			.body("visibility", equalTo("PUBLIC"))
			.body("teamCountLimit", equalTo(8));
	}

	@Test
	void getReturnsNotFoundForUnknownTournament() {
		given()
		.when()
			.get("/api/tournaments/{id}", 999999)
		.then()
			.statusCode(404)
			.body("status", equalTo(404))
			.body("message", equalTo("No tournament with id 999999 was found."));
	}

	@Test
	void listsTournamentsPaginated() {
		long first = createTournament();
		long second = createTournament();
		long deleted = createTournament();

		authenticated()
		.when()
			.delete("/api/tournaments/{id}", deleted)
		.then()
			.statusCode(204);

		int total = given()
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("page", equalTo(0))
			.body("size", equalTo(20))
			.body("content.id", not(hasItems((int) deleted)))
			.extract().jsonPath().getInt("totalElements");

		// Ordered by id, so the last two remaining tournaments fall on the final page of size 1
		given()
			.queryParam("page", total - 2)
			.queryParam("size", 1)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("content.id", contains((int) first))
			.body("totalPages", equalTo(total));

		given()
			.queryParam("page", total - 1)
			.queryParam("size", 1)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("content.id", contains((int) second));
	}

	@Test
	void returnsEmptyPageBeyondLastPage() {
		createTournament();

		given()
			.queryParam("page", 100000)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("content", empty())
			.body("page", equalTo(100000));
	}

	@Test
	void writesRequireToken() {
		long id = createTournament();

		given().contentType(ContentType.JSON).body(VALID_BODY)
			.when().post("/api/tournaments")
			.then().statusCode(401);

		given().contentType(ContentType.JSON).body("{\"name\": \"x\"}")
			.when().put("/api/tournaments/{id}", id)
			.then().statusCode(401);

		given()
			.when().delete("/api/tournaments/{id}", id)
			.then().statusCode(401);

		// Reads stay public
		given()
			.when().get("/api/tournaments/{id}", id)
			.then().statusCode(200);
	}

	@Test
	void createSetsCurrentUserAsOwner() {
		long id = createTournament();
		long ownerId = authenticated().get("/api/auth/me").then().extract().jsonPath().getLong("id");

		given()
		.when()
			.get("/api/tournaments/{id}", id)
		.then()
			.statusCode(200)
			.body("ownerId", equalTo((int) ownerId));
	}

	@Test
	void ignoresOwnerIdInRequestBody() {
		long id = authenticated()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "Copa", "description": "d", "visibility": "PUBLIC", "teamCountLimit": 8, "ownerId": 999999}
				""")
		.when()
			.post("/api/tournaments")
		.then()
			.statusCode(201)
			.body("ownerId", not(equalTo(999999)))
			.extract().jsonPath().getLong("id");

		authenticated()
			.contentType(ContentType.JSON)
			.body("{\"ownerId\": 999999}")
		.when()
			.put("/api/tournaments/{id}", id)
		.then()
			.statusCode(200)
			.body("ownerId", not(equalTo(999999)));
	}

	@Test
	void onlyOwnerCanModifyPublicTournament() {
		long id = createTournament();
		var otherToken = TestUsers.newToken();

		given()
			.auth().oauth2(otherToken)
			.contentType(ContentType.JSON)
			.body("{\"name\": \"Invadido\"}")
		.when()
			.put("/api/tournaments/{id}", id)
		.then()
			.statusCode(403)
			.body("status", equalTo(403));

		given()
			.auth().oauth2(otherToken)
		.when()
			.delete("/api/tournaments/{id}", id)
		.then()
			.statusCode(403);

		given()
		.when()
			.get("/api/tournaments/{id}", id)
		.then()
			.statusCode(200)
			.body("name", equalTo("Copa"));
	}

	@Test
	void privateTournamentIsHiddenFromEveryoneButOwner() {
		long id = createPrivateTournament();
		var otherToken = TestUsers.newToken();

		authenticated()
		.when()
			.get("/api/tournaments/{id}", id)
		.then()
			.statusCode(200);

		given()
		.when()
			.get("/api/tournaments/{id}", id)
		.then()
			.statusCode(404)
			.body("message", equalTo("No tournament with id %d was found.".formatted(id)));

		given()
			.auth().oauth2(otherToken)
		.when()
			.get("/api/tournaments/{id}", id)
		.then()
			.statusCode(404);

		// Writes by non-owners also get 404, not 403, so the tournament's existence isn't revealed
		given()
			.auth().oauth2(otherToken)
			.contentType(ContentType.JSON)
			.body("{\"name\": \"x\"}")
		.when()
			.put("/api/tournaments/{id}", id)
		.then()
			.statusCode(404);

		given()
			.auth().oauth2(otherToken)
		.when()
			.delete("/api/tournaments/{id}", id)
		.then()
			.statusCode(404);
	}

	@Test
	void listShowsPrivateTournamentsOnlyToOwner() {
		long publicId = createTournament();
		long privateId = createPrivateTournament();
		var otherToken = TestUsers.newToken();

		given()
			.queryParam("size", 100)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("content.id", hasItems((int) publicId))
			.body("content.id", not(hasItems((int) privateId)))
			.body("content.visibility", everyItem(equalTo("PUBLIC")));

		given()
			.auth().oauth2(otherToken)
			.queryParam("size", 100)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("content.id", not(hasItems((int) privateId)));

		// The list is ordered by id, so the newest tournaments sit on the last page
		int total = authenticated()
			.get("/api/tournaments").then().extract().jsonPath().getInt("totalElements");
		authenticated()
			.queryParam("page", total - 1)
			.queryParam("size", 1)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(200)
			.body("content.id", contains((int) privateId));
	}

	@Test
	void rejectsInvalidPaginationParams() {
		given()
			.queryParam("page", -1)
			.queryParam("size", 101)
		.when()
			.get("/api/tournaments")
		.then()
			.statusCode(400)
			.body("message", containsString("page"))
			.body("message", containsString("size"));
	}
}
