package com.brakit.tournament.service;

import com.brakit.tournament.dto.request.CreateTournamentRequest;
import com.brakit.tournament.dto.request.CreateTournamentRequestMapper;
import com.brakit.tournament.dto.request.UpdateTournamentRequest;
import com.brakit.tournament.dto.request.UpdateTournamentRequestMapper;
import com.brakit.tournament.dto.response.CreateTournamentResponse;
import com.brakit.tournament.dto.response.CreateTournamentResponseMapper;
import com.brakit.tournament.model.Tournament;
import com.brakit.tournament.repository.TournamentRepository;
import com.brakit.tournament.service.exception.TournamentNotFound;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped 
public class TournamentService {
	
	@Inject TournamentRepository tournamentRepository;

	@Transactional 
	public CreateTournamentResponse createTournament(CreateTournamentRequest request) {
		var tournament = CreateTournamentRequestMapper.toModel(request);
		var savedTournament = tournamentRepository.save(tournament);
		return CreateTournamentResponseMapper.toResponse(savedTournament);
	}

	@Transactional
	public CreateTournamentResponse updateTournament(UpdateTournamentRequest request) {
		var model = tournamentRepository.getById(request.id())
				.orElseThrow(() -> new TournamentNotFound(request.id()))
				.merge(UpdateTournamentRequestMapper.toModel(request));
		var saved = tournamentRepository.save(model);
		return CreateTournamentResponseMapper.toResponse(saved);
	}
}
