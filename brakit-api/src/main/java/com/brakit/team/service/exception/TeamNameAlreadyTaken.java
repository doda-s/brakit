package com.brakit.team.service.exception;

public class TeamNameAlreadyTaken extends RuntimeException {
	public TeamNameAlreadyTaken(String name) {
		super("A team named %s already exists in this tournament.".formatted(name));
	}
}
