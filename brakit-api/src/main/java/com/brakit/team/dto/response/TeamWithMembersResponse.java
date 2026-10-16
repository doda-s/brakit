package com.brakit.team.dto.response;

import java.util.List;

import com.brakit.member.dto.response.MemberResponse;

// Returned on team creation so the client gets the ids of the members created alongside the team
public record TeamWithMembersResponse(
	Long id,
	Long tournamentId,
	String name,
	List<MemberResponse> members
) {}
