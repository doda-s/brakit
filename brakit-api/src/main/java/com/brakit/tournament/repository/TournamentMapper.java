package com.brakit.tournament.repository;

import com.brakit.tournament.entity.TournamentEntity;
import com.brakit.tournament.model.Tournament;

public class TournamentMapper {
	public static Tournament toModel(TournamentEntity entity) {
		return new Tournament(
			entity.getId(),
			entity.getName(),
			entity.getDescription(),
			entity.getVisibility(),
			entity.getTeamCountLimit()
		);
	}

	public static TournamentEntity toEntity(Tournament model) {
		return new TournamentEntity(
			model.getId(),
			model.getName(),
			model.getDescription(),
			model.getVisibility(),
			model.getTeamCountLimit()
		);
	}
}
