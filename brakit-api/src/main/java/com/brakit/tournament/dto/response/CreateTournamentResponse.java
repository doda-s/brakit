package com.brakit.tournament.dto.response;

import com.brakit.tournament.model.TournamentVisibility;

public record CreateTournamentResponse(
	Long id,
	String name,
	String description,
	TournamentVisibility visibility,
	int teamCountLimit
) {}
