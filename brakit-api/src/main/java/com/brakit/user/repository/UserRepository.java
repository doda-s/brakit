package com.brakit.user.repository;

import java.util.Optional;

import com.brakit.user.entity.UserEntity;
import com.brakit.user.model.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {

	public User save(User model) {
		UserEntity entity = UserMapper.toEntity(model);
		var savedEntity = this.getEntityManager().merge(entity);
		return UserMapper.toModel(savedEntity);
	}

	public Optional<User> getById(Long id) {
		var entity = this.findByIdOptional(id);
		return entity.map(UserMapper::toModel);
	}

	public Optional<User> getByEmail(String email) {
		var entity = this.find("email", email).firstResultOptional();
		return entity.map(UserMapper::toModel);
	}

	public boolean existsByEmail(String email) {
		return this.count("email", email) > 0;
	}
}
