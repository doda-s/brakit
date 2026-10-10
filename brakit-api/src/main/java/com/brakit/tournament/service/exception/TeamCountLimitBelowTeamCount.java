package com.brakit.tournament.service.exception;

public class TeamCountLimitBelowTeamCount extends RuntimeException {
	public TeamCountLimitBelowTeamCount(int teamCountLimit, long teamCount) {
		super("Team count limit %d is below the %d teams already registered.".formatted(teamCountLimit, teamCount));
	}
}
