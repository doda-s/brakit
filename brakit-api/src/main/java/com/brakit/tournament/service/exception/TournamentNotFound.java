package com.brakit.tournament.service.exception;

public class TournamentNotFound extends RuntimeException {
	public TournamentNotFound(Long id) {
		super("No tournament with id %s was found.".formatted(id));
	}
}
