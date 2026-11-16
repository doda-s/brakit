package com.brakit.support;

import static io.restassured.RestAssured.given;

import java.util.UUID;

import io.restassured.http.ContentType;

public class TestUsers {

	public record TestUser(String token, String email) {}

	// Registers a fresh user and returns its access token
	public static String newToken() {
		return newUser().token();
	}

	// Registers a fresh user and returns its access token and e-mail
	public static TestUser newUser() {
		var email = "user-%s@brakit.com".formatted(UUID.randomUUID());
		var credentials = """
			{"name": "Organizador", "email": "%s", "password": "s3cret-password"}
			""".formatted(email);
		given().contentType(ContentType.JSON).body(credentials)
			.post("/api/auth/register").then().statusCode(201);
		var token = given().contentType(ContentType.JSON).body(credentials)
			.post("/api/auth/login").then().statusCode(200)
			.extract().jsonPath().getString("accessToken");
		return new TestUser(token, email);
	}
}
