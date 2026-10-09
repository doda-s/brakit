package com.brakit.tournament.service;

import java.util.Optional;

import com.brakit.pagination.Page;
import com.brakit.tournament.dto.request.CreateTournamentRequest;
import com.brakit.tournament.dto.request.CreateTournamentRequestMapper;
import com.brakit.tournament.dto.request.UpdateTournamentRequest;
import com.brakit.tournament.dto.request.UpdateTournamentRequestMapper;
import com.brakit.tournament.dto.response.TournamentResponse;
import com.brakit.tournament.dto.response.TournamentResponseMapper;
import com.brakit.tournament.model.Tournament;
import com.brakit.tournament.repository.TournamentRepository;
import com.brakit.tournament.service.exception.NotTournamentOwner;
import com.brakit.tournament.service.exception.TournamentNotFound;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped 
public class TournamentService {
	
	@Inject TournamentRepository tournamentRepository;

	public Page<TournamentResponse> listTournaments(Optional<Long> viewerId, int page, int size) {
		return tournamentRepository.getVisiblePage(viewerId, page, size)
				.map(TournamentResponseMapper::toResponse);
	}

	public TournamentResponse getTournament(Long id, Optional<Long> viewerId) {
		return TournamentResponseMapper.toResponse(getVisible(id, viewerId));
	}

	@Transactional 
	public TournamentResponse createTournament(CreateTournamentRequest request, Long ownerId) {
		var tournament = CreateTournamentRequestMapper.toModel(request, ownerId);
		var savedTournament = tournamentRepository.save(tournament);
		return TournamentResponseMapper.toResponse(savedTournament);
	}

	@Transactional
	public TournamentResponse updateTournament(Long id, UpdateTournamentRequest request, Long userId) {
		var model = getOwned(id, userId)
				.merge(UpdateTournamentRequestMapper.toModel(id, request));
		var saved = tournamentRepository.save(model);
		return TournamentResponseMapper.toResponse(saved);
	}

	@Transactional
	public void deleteTournament(Long id, Long userId) {
		getOwned(id, userId);
		tournamentRepository.deleteById(id);
	}

	// A private tournament is reported as not found to anyone but its owner, so its existence isn't leaked
	private Tournament getVisible(Long id, Optional<Long> viewerId) {
		return tournamentRepository.getById(id)
				.filter(t -> t.isVisibleTo(viewerId))
				.orElseThrow(() -> new TournamentNotFound(id));
	}

	private Tournament getOwned(Long id, Long userId) {
		var tournament = getVisible(id, Optional.of(userId));
		if (!tournament.isOwnedBy(userId)) {
			throw new NotTournamentOwner(id);
		}
		return tournament;
	}
}
