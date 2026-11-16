package com.brakit.coowner.service.exception;

public class AlreadyTournamentOwner extends RuntimeException {
	public AlreadyTournamentOwner(String email) {
		super("User %s is already an owner of this tournament.".formatted(email));
	}
}
