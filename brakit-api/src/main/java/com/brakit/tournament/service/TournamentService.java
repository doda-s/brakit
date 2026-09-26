package com.brakit.tournament.service;

import com.brakit.tournament.dto.request.CreateTournamentRequest;
import com.brakit.tournament.dto.request.CreateTournamentRequestMapper;
import com.brakit.tournament.dto.response.CreateTournamentResponse;
import com.brakit.tournament.dto.response.CreateTournamentResponseMapper;
import com.brakit.tournament.model.Tournament;
import com.brakit.tournament.repository.TournamentRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped 
public class TournamentService {
	
	@Inject TournamentRepository tournamentRepository;

	@Transactional 
	public CreateTournamentResponse createTournament(CreateTournamentRequest request) {
		var tournament = CreateTournamentRequestMapper.toModel(request);
		var savedTournament = tournamentRepository.save(Tournament.create(
			tournament.getName(),
			tournament.getDescription(),
			tournament.getVisibility(),
			tournament.getTeamCountLimit()
		));
		return CreateTournamentResponseMapper.toResponse(savedTournament);
	}	
}
