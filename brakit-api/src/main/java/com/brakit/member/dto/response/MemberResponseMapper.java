package com.brakit.member.dto.response;

import com.brakit.member.model.TeamMember;

public class MemberResponseMapper {
	public static MemberResponse toResponse(TeamMember model) {
		return new MemberResponse(
			model.getId(),
			model.getTeamId(),
			model.getName(),
			model.getRole()
		);
	}
}
