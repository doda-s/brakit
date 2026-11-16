package com.brakit.tournament.dto.response;

import com.brakit.tournament.model.TournamentRole;
import com.brakit.tournament.model.TournamentVisibility;

public record TournamentResponse(
	Long id,
	String name,
	String description,
	TournamentVisibility visibility,
	int teamCountLimit,
	Long ownerId,
	// The requesting user's role, so clients know which actions to offer; null for anyone but the owners
	TournamentRole viewerRole
) {}
