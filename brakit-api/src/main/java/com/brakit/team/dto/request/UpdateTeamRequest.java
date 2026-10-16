package com.brakit.team.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Name is the only editable field, so it is required rather than optional
public record UpdateTeamRequest(
	@NotBlank @Size(max = 255) String name
) {}
