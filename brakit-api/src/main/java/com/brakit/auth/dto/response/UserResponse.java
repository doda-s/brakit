package com.brakit.auth.dto.response;

public record UserResponse(
	Long id,
	String name,
	String email
) {}
