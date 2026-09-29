package com.brakit.auth.dto.response;

import com.brakit.user.model.User;

public class UserResponseMapper {
	public static UserResponse toResponse(User model) {
		return new UserResponse(
			model.getId(),
			model.getName(),
			model.getEmail()
		);
	}
}
