package com.brakit.support;

import static io.restassured.RestAssured.given;

import java.util.UUID;

import io.restassured.http.ContentType;

public class TestUsers {

	// Registers a fresh user and returns its access token
	public static String newToken() {
		var credentials = """
			{"name": "Organizador", "email": "user-%s@brakit.com", "password": "s3cret-password"}
			""".formatted(UUID.randomUUID());
		given().contentType(ContentType.JSON).body(credentials)
			.post("/api/auth/register").then().statusCode(201);
		return given().contentType(ContentType.JSON).body(credentials)
			.post("/api/auth/login").then().statusCode(200)
			.extract().jsonPath().getString("accessToken");
	}
}
