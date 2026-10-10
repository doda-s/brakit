package com.brakit.team.dto.request;

import java.util.List;

import com.brakit.member.dto.request.MemberRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
	@NotBlank @Size(max = 255) String name,
	// Optional; creates the team and its initial members atomically. Larger teams grow through the members endpoint
	@Size(max = 20) List<@Valid @NotNull MemberRequest> members
) {
	public CreateTeamRequest {
		members = members != null ? members : List.of();
	}
}
