package com.brakit.tournament.dto.request;

import com.brakit.tournament.model.TournamentVisibility;

import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateTournamentRequest(
	@NotBlank String name,
	@NotBlank String description,
	@NotNull  TournamentVisibility visibility,
	@NotNull @Positive int teamCountLimit
) {}
