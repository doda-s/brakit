package com.brakit.auth.service.exception;

public class EmailAlreadyRegistered extends RuntimeException {
	public EmailAlreadyRegistered(String email) {
		super("Email %s is already registered.".formatted(email));
	}
}
