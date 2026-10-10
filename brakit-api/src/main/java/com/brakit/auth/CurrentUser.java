package com.brakit.auth;

import java.util.Optional;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

// Exposes the id of the user behind the current request's JWT (its subject claim)
@RequestScoped
public class CurrentUser {

	@Inject JsonWebToken jwt;

	// Empty for anonymous requests on public endpoints
	public Optional<Long> id() {
		return Optional.ofNullable(jwt.getSubject()).map(Long::valueOf);
	}

	// Only call from @Authenticated endpoints, where a token is guaranteed
	public Long requireId() {
		return id().orElseThrow(() -> new IllegalStateException("No authenticated user in the current request"));
	}
}
