package com.brakit.user.repository;

import com.brakit.user.entity.UserEntity;
import com.brakit.user.model.User;

public class UserMapper {
	public static User toModel(UserEntity entity) {
		return new User(
			entity.getId(),
			entity.getName(),
			entity.getEmail(),
			entity.getPasswordHash()
		);
	}

	public static UserEntity toEntity(User model) {
		return new UserEntity(
			model.getId(),
			model.getName(),
			model.getEmail(),
			model.getPasswordHash()
		);
	}
}
