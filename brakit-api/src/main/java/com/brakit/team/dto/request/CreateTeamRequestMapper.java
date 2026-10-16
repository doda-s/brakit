package com.brakit.team.dto.request;

import com.brakit.team.model.Team;

public class CreateTeamRequestMapper {
	public static Team toModel(CreateTeamRequest request, Long tournamentId) {
		return Team.create(
			tournamentId,
			request.name());
	}
}
