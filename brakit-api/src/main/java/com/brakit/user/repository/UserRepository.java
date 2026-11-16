package com.brakit.user.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.brakit.user.entity.UserEntity;
import com.brakit.user.model.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
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

	// Ordered by name, then id
	public List<User> getAllByIds(Collection<Long> ids) {
		if (ids.isEmpty()) {
			return List.of();
		}
		return this.list("id in ?1", Sort.by("name").and("id"), ids).stream()
				.map(UserMapper::toModel)
				.toList();
	}

	public boolean existsByEmail(String email) {
		return this.count("email", email) > 0;
	}
}
