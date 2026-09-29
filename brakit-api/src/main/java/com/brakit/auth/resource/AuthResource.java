package com.brakit.auth.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.brakit.auth.dto.request.LoginRequest;
import com.brakit.auth.dto.request.RegisterRequest;
import com.brakit.auth.service.AuthService;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
public class AuthResource {

	@Inject AuthService authService;
	@Inject JsonWebToken jwt;

	@POST()
	@Path("/register")
	public Response register(@Valid @NotNull RegisterRequest request) {
		var response = authService.register(request);
		return Response.status(Response.Status.CREATED).entity(response).build();
	}

	@POST()
	@Path("/login")
	public Response login(@Valid @NotNull LoginRequest request) {
		var response = authService.login(request);
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@GET()
	@Path("/me")
	@Authenticated
	public Response me() {
		var response = authService.getUser(Long.valueOf(jwt.getSubject()));
		return Response.status(Response.Status.OK).entity(response).build();
	}
}
