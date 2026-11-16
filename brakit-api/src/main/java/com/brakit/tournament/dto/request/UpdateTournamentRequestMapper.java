package com.brakit.tournament.dto.request;

import java.util.Set;

import com.brakit.tournament.model.Tournament;

public class UpdateTournamentRequestMapper {
	public static Tournament toModel(Long id, UpdateTournamentRequest request) {
		return new Tournament(
			id,
			request.name().orElse(null),
			request.description().orElse(null),
			request.visibility().orElse(null),
			request.teamCountLimit().orElse(0),
			null,
			// Co-owners are managed through their own endpoints and never merged from an update
			Set.of());
	}
}
