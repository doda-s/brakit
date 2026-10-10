package com.brakit.team.dto.response;

import com.brakit.team.model.Team;

public class TeamResponseMapper {
	public static TeamResponse toResponse(Team model) {
		return new TeamResponse(
			model.getId(),
			model.getTournamentId(),
			model.getName()
		);
	}
}
