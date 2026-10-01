package com.brakit.tournament.dto.request;

import java.util.Optional;

import com.brakit.tournament.model.TournamentVisibility;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record UpdateTournamentRequest(
	Optional<@Pattern(regexp = NOT_BLANK, message = "must not be blank") String> name,
	Optional<@Pattern(regexp = NOT_BLANK, message = "must not be blank") String> description,
	Optional<TournamentVisibility> visibility,
	Optional<@Positive Integer> teamCountLimit
) {
	// Unlike @NotBlank, @Pattern accepts null, so an absent field is still valid
	private static final String NOT_BLANK = "(?s).*\\S.*";

	public UpdateTournamentRequest {
		name = name != null ? name : Optional.empty();
		description = description != null ? description : Optional.empty();
		visibility = visibility != null ? visibility : Optional.empty();
		teamCountLimit = teamCountLimit != null ? teamCountLimit : Optional.empty();
	}
}
