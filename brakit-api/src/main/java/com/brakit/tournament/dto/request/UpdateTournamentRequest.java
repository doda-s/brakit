package com.brakit.tournament.dto.request;

import java.util.Optional;

import com.brakit.tournament.model.TournamentVisibility;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateTournamentRequest(
	@NotNull @Positive long id,
	Optional<String> name,
	Optional<String> description,
	Optional<TournamentVisibility> visibility,
	Optional<Integer> teamCountLimit
) {}
