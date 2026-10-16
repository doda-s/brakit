package com.brakit.tournament.dto.request;

import com.brakit.tournament.model.Tournament;

public class CreateTournamentRequestMapper {
	public static Tournament toModel(CreateTournamentRequest request, Long ownerId) {
		return Tournament.create(
			request.name(),
			request.description(),
			request.visibility(),
			request.teamCountLimit(),
			ownerId);
	}
}
