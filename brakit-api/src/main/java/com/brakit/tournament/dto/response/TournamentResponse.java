package com.brakit.tournament.dto.response;

import com.brakit.tournament.model.TournamentVisibility;

public record TournamentResponse(
	Long id,
	String name,
	String description,
	TournamentVisibility visibility,
	int teamCountLimit
) {}
