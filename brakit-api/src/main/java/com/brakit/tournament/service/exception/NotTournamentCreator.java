package com.brakit.tournament.service.exception;

public class NotTournamentCreator extends RuntimeException {
	public NotTournamentCreator(Long id) {
		super("Only the creator can delete tournament %s or manage its co-owners.".formatted(id));
	}
}
