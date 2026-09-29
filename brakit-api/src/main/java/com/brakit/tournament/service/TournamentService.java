package com.brakit.tournament.service;

import com.brakit.pagination.Page;
import com.brakit.tournament.dto.request.CreateTournamentRequest;
import com.brakit.tournament.dto.request.CreateTournamentRequestMapper;
import com.brakit.tournament.dto.request.UpdateTournamentRequest;
import com.brakit.tournament.dto.request.UpdateTournamentRequestMapper;
import com.brakit.tournament.dto.response.TournamentResponse;
import com.brakit.tournament.dto.response.TournamentResponseMapper;
import com.brakit.tournament.repository.TournamentRepository;
import com.brakit.tournament.service.exception.TournamentNotFound;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped 
public class TournamentService {
	
	@Inject TournamentRepository tournamentRepository;

	public Page<TournamentResponse> listTournaments(int page, int size) {
		return tournamentRepository.getPage(page, size)
				.map(TournamentResponseMapper::toResponse);
	}

	public TournamentResponse getTournament(Long id) {
		return tournamentRepository.getById(id)
				.map(TournamentResponseMapper::toResponse)
				.orElseThrow(() -> new TournamentNotFound(id));
	}

	@Transactional 
	public TournamentResponse createTournament(CreateTournamentRequest request) {
		var tournament = CreateTournamentRequestMapper.toModel(request);
		var savedTournament = tournamentRepository.save(tournament);
		return TournamentResponseMapper.toResponse(savedTournament);
	}

	@Transactional
	public TournamentResponse updateTournament(Long id, UpdateTournamentRequest request) {
		var model = tournamentRepository.getById(id)
				.orElseThrow(() -> new TournamentNotFound(id))
				.merge(UpdateTournamentRequestMapper.toModel(id, request));
		var saved = tournamentRepository.save(model);
		return TournamentResponseMapper.toResponse(saved);
	}

	@Transactional
	public void deleteTournament(Long id) {
		if (!tournamentRepository.deleteById(id)) {
			throw new TournamentNotFound(id);
		}
	}
}
