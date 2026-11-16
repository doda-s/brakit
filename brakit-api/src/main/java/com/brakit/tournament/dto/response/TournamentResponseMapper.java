package com.brakit.tournament.dto.response;

import java.util.Optional;

import com.brakit.tournament.model.Tournament;

public class TournamentResponseMapper {
	public static TournamentResponse toResponse(Tournament model, Optional<Long> viewerId) {
		return new TournamentResponse(
			model.getId(),
			model.getName(),
			model.getDescription(),
			model.getVisibility(),
			model.getTeamCountLimit(),
			model.getOwnerId(),
			viewerId.flatMap(model::roleOf).orElse(null)
		);
	}
}
