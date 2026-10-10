package com.brakit.team.dto.response;

import java.util.List;

import com.brakit.member.dto.response.MemberResponse;
import com.brakit.team.model.Team;

public class TeamResponseMapper {
	public static TeamResponse toResponse(Team model) {
		return new TeamResponse(
			model.getId(),
			model.getTournamentId(),
			model.getName()
		);
	}

	public static TeamWithMembersResponse toResponse(Team model, List<MemberResponse> members) {
		return new TeamWithMembersResponse(
			model.getId(),
			model.getTournamentId(),
			model.getName(),
			members
		);
	}
}
