package com.brakit.tournament.dto.request;

import com.brakit.tournament.model.Tournament;

public class UpdateTournamentRequestMapper {
	public static Tournament toModel(UpdateTournamentRequest request) {
		return new Tournament(
			request.id(),
			request.name().orElse(null),
			request.description().orElse(null),
			request.visibility().orElse(null),
			request.teamCountLimit().orElse(0));
	}
}
