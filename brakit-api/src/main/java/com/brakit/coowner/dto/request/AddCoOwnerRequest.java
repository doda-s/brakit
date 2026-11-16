package com.brakit.coowner.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// The co-owner is picked by the e-mail they registered with
public record AddCoOwnerRequest(
	@NotBlank @Email String email
) {}
