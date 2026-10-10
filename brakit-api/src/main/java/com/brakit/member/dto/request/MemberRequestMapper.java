package com.brakit.member.dto.request;

import com.brakit.member.model.TeamMember;

public class MemberRequestMapper {
	public static TeamMember toModel(MemberRequest request, Long teamId) {
		return TeamMember.create(
			teamId,
			request.name(),
			request.role());
	}
}
