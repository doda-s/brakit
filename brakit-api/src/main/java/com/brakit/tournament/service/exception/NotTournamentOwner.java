package com.brakit.tournament.service.exception;

public class NotTournamentOwner extends RuntimeException {
	public NotTournamentOwner(Long id) {
		super("Only the owner can modify tournament %s.".formatted(id));
	}
}
