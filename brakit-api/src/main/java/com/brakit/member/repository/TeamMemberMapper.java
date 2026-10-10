package com.brakit.member.repository;

import com.brakit.member.entity.TeamMemberEntity;
import com.brakit.member.model.TeamMember;

public class TeamMemberMapper {
	public static TeamMember toModel(TeamMemberEntity entity) {
		return new TeamMember(
			entity.getId(),
			entity.getTeamId(),
			entity.getName(),
			entity.getRole()
		);
	}

	public static TeamMemberEntity toEntity(TeamMember model) {
		return new TeamMemberEntity(
			model.getId(),
			model.getTeamId(),
			model.getName(),
			model.getRole()
		);
	}
}
