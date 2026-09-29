package com.brakit.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
	@NotBlank String name,
	@NotBlank @Email String email,
	// BCrypt only considers the first 72 bytes of the password
	@NotNull @Size(min = 8, max = 72) String password
) {}
