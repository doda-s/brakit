package com.brakit.auth.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.brakit.auth.dto.request.LoginRequest;
import com.brakit.auth.dto.request.RegisterRequest;
import com.brakit.auth.dto.request.RegisterRequestMapper;
import com.brakit.auth.dto.response.TokenResponse;
import com.brakit.auth.dto.response.UserResponse;
import com.brakit.auth.dto.response.UserResponseMapper;
import com.brakit.auth.service.exception.EmailAlreadyRegistered;
import com.brakit.auth.service.exception.InvalidCredentials;
import com.brakit.auth.service.exception.UserNotFound;
import com.brakit.user.model.User;
import com.brakit.user.repository.UserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

	@Inject UserRepository userRepository;

	@ConfigProperty(name = "smallrye.jwt.new-token.lifespan") long tokenLifespan;

	@Transactional
	public UserResponse register(RegisterRequest request) {
		var email = User.normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyRegistered(email);
		}
		var user = RegisterRequestMapper.toModel(request, BcryptUtil.bcryptHash(request.password()));
		var savedUser = userRepository.save(user);
		return UserResponseMapper.toResponse(savedUser);
	}

	public TokenResponse login(LoginRequest request) {
		var user = userRepository.getByEmail(User.normalizeEmail(request.email()))
				.filter(u -> BcryptUtil.matches(request.password(), u.getPasswordHash()))
				.orElseThrow(InvalidCredentials::new);
		// Issuer and lifespan come from the smallrye.jwt.new-token.* config
		var token = Jwt.subject(user.getId().toString())
				.upn(user.getEmail())
				.groups("user")
				.sign();
		return new TokenResponse(token, "Bearer", tokenLifespan);
	}

	public UserResponse getUser(Long id) {
		return userRepository.getById(id)
				.map(UserResponseMapper::toResponse)
				.orElseThrow(() -> new UserNotFound(id));
	}
}
