package com.brakit.tournament.dto.response;

import com.brakit.tournament.model.Tournament;

public class TournamentResponseMapper {
	public static TournamentResponse toResponse(Tournament model) {
		return new TournamentResponse(
			model.getId(),
			model.getName(),
			model.getDescription(),
			model.getVisibility(),
			model.getTeamCountLimit()
		);
	}
}
