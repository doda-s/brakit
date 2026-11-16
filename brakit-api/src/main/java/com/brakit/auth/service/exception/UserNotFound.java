package com.brakit.auth.service.exception;

public class UserNotFound extends RuntimeException {
	public UserNotFound(Long id) {
		super("No user with id %s was found.".formatted(id));
	}

	public UserNotFound(String email) {
		super("No user with email %s was found.".formatted(email));
	}
}
