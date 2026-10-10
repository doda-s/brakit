package com.brakit.member.dto.response;

public record MemberResponse(
	Long id,
	Long teamId,
	String name,
	String role
) {}
