package com.brakit.tournament.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class TournamentResourceTest {

	private static final String VALID_BODY = """
		{"name": "Copa", "description": "Torneio de teste", "visibility": "PUBLIC", "teamCountLimit": 8}
		""";

	private long createTournament() {
		return given()
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
		given()
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
		given()
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

		given()
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

		given()
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

		given()
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
		given()
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
}
