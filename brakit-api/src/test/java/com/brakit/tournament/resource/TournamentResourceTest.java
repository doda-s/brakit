package com.brakit.tournament.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@QuarkusTest
class TournamentResourceTest {

	private static final String VALID_BODY = """
		{"name": "Copa", "description": "Torneio de teste", "visibility": "PUBLIC", "teamCountLimit": 8}
		""";

	private static String token;

	// Registers one user per test class and reuses its token for every write request
	private static RequestSpecification authenticated() {
		if (token == null) {
			var email = "tournament-" + UUID.randomUUID() + "@brakit.com";
			var credentials = """
				{"name": "Organizador", "email": "%s", "password": "s3cret-password"}
				""".formatted(email);
			given().contentType(ContentType.JSON).body(credentials)
				.post("/api/auth/register").then().statusCode(201);
			token = given().contentType(ContentType.JSON).body(credentials)
				.post("/api/auth/login").then().statusCode(200)
				.extract().jsonPath().getString("accessToken");
		}
		return given().auth().oauth2(token);
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
