package com.brakit.tournament.dto.response;

import com.brakit.tournament.model.Tournament;

public class CreateTournamentResponseMapper {
	public static CreateTournamentResponse toResponse(Tournament model) {
		return new CreateTournamentResponse(
			model.getId(),
			model.getName(),
			model.getDescription(),
			model.getVisibility(),
			model.getTeamCountLimit()
		);
	}
}
