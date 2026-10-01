package com.brakit.auth.dto.request;

import com.brakit.user.model.User;

public class RegisterRequestMapper {
	public static User toModel(RegisterRequest request, String passwordHash) {
		return User.create(
			request.name(),
			request.email(),
			passwordHash);
	}
}
