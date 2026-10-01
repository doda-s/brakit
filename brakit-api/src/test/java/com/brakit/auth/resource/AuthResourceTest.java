package com.brakit.auth.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class AuthResourceTest {

	private static final String PASSWORD = "s3cret-password";

	private String uniqueEmail() {
		return "user-" + UUID.randomUUID() + "@brakit.com";
	}

	private long register(String email) {
		return given()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "Fulano", "email": "%s", "password": "%s"}
				""".formatted(email, PASSWORD))
		.when()
			.post("/api/auth/register")
		.then()
			.statusCode(201)
			.extract().jsonPath().getLong("id");
	}

	private String login(String email) {
		return given()
			.contentType(ContentType.JSON)
			.body("""
				{"email": "%s", "password": "%s"}
				""".formatted(email, PASSWORD))
		.when()
			.post("/api/auth/login")
		.then()
			.statusCode(200)
			.extract().jsonPath().getString("accessToken");
	}

	@Test
	void registersUserWithoutExposingPassword() {
		var email = uniqueEmail();

		given()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "Fulano", "email": "%s", "password": "%s"}
				""".formatted(email, PASSWORD))
		.when()
			.post("/api/auth/register")
		.then()
			.statusCode(201)
			.body("id", notNullValue())
			.body("name", equalTo("Fulano"))
			.body("email", equalTo(email))
			.body("$", not(hasKey("password")))
			.body("$", not(hasKey("passwordHash")));
	}

	@Test
	void rejectsDuplicateEmailIgnoringCase() {
		var email = uniqueEmail();
		register(email);

		given()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "Outro", "email": "%s", "password": "%s"}
				""".formatted(email.toUpperCase(), PASSWORD))
		.when()
			.post("/api/auth/register")
		.then()
			.statusCode(409)
			.body("status", equalTo(409));
	}

	@Test
	void rejectsInvalidRegisterRequest() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{"name": "", "email": "not-an-email", "password": "short"}
				""")
		.when()
			.post("/api/auth/register")
		.then()
			.statusCode(400)
			.body("message", containsString("name"))
			.body("message", containsString("email"))
			.body("message", containsString("password"));
	}

	@Test
	void logsInAndReturnsBearerToken() {
		var email = uniqueEmail();
		register(email);

		given()
			.contentType(ContentType.JSON)
			.body("""
				{"email": "%s", "password": "%s"}
				""".formatted(email.toUpperCase(), PASSWORD))
		.when()
			.post("/api/auth/login")
		.then()
			.statusCode(200)
			.body("accessToken", notNullValue())
			.body("tokenType", equalTo("Bearer"))
			.body("expiresIn", equalTo(43200));
	}

	@Test
	void rejectsWrongPassword() {
		var email = uniqueEmail();
		register(email);

		given()
			.contentType(ContentType.JSON)
			.body("""
				{"email": "%s", "password": "wrong-password"}
				""".formatted(email))
		.when()
			.post("/api/auth/login")
		.then()
			.statusCode(401)
			.body("message", equalTo("Invalid email or password."));
	}

	@Test
	void rejectsUnknownEmailWithSameMessage() {
		given()
			.contentType(ContentType.JSON)
			.body("""
				{"email": "%s", "password": "%s"}
				""".formatted(uniqueEmail(), PASSWORD))
		.when()
			.post("/api/auth/login")
		.then()
			.statusCode(401)
			.body("message", equalTo("Invalid email or password."));
	}

	@Test
	void meReturnsAuthenticatedUser() {
		var email = uniqueEmail();
		long id = register(email);
		var token = login(email);

		given()
			.auth().oauth2(token)
		.when()
			.get("/api/auth/me")
		.then()
			.statusCode(200)
			.body("id", equalTo((int) id))
			.body("email", equalTo(email));
	}

	@Test
	void meRequiresToken() {
		given()
		.when()
			.get("/api/auth/me")
		.then()
			.statusCode(401);
	}

	@Test
	void meRejectsTamperedToken() {
		var email = uniqueEmail();
		register(email);
		var token = login(email);
		var parts = token.split("\\.");
		var tampered = parts[0] + "." + parts[1] + "." + new StringBuilder(parts[2]).reverse();

		given()
			.auth().oauth2(tampered)
		.when()
			.get("/api/auth/me")
		.then()
			.statusCode(401);
	}
}
