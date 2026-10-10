package com.brakit.team.repository;

import com.brakit.team.entity.TeamEntity;
import com.brakit.team.model.Team;

public class TeamMapper {
	public static Team toModel(TeamEntity entity) {
		return new Team(
			entity.getId(),
			entity.getTournamentId(),
			entity.getName()
		);
	}

	public static TeamEntity toEntity(Team model) {
		return new TeamEntity(
			model.getId(),
			model.getTournamentId(),
			model.getName()
		);
	}
}
