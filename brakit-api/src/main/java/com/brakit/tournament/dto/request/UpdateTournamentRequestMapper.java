package com.brakit.tournament.dto.request;

import com.brakit.tournament.model.Tournament;

public class UpdateTournamentRequestMapper {
	public static Tournament toModel(Long id, UpdateTournamentRequest request) {
		return new Tournament(
			id,
			request.name().orElse(null),
			request.description().orElse(null),
			request.visibility().orElse(null),
			request.teamCountLimit().orElse(0));
	}
}
