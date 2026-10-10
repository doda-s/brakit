package com.brakit.team.service.exception;

public class TournamentFull extends RuntimeException {
	public TournamentFull(int teamCountLimit) {
		super("Tournament already has the maximum of %d teams.".formatted(teamCountLimit));
	}
}
