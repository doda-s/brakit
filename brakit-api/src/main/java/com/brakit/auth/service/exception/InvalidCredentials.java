package com.brakit.auth.service.exception;

public class InvalidCredentials extends RuntimeException {
	public InvalidCredentials() {
		super("Invalid email or password.");
	}
}
