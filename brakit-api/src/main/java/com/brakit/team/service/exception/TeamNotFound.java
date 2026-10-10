package com.brakit.team.service.exception;

public class TeamNotFound extends RuntimeException {
	public TeamNotFound(Long id) {
		super("No team with id %s was found.".formatted(id));
	}
}
