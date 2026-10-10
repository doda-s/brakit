package com.brakit.tournament.service;

import java.util.Optional;

import com.brakit.pagination.Page;
import com.brakit.team.repository.TeamRepository;
import com.brakit.tournament.dto.request.CreateTournamentRequest;
import com.brakit.tournament.dto.request.CreateTournamentRequestMapper;
import com.brakit.tournament.dto.request.UpdateTournamentRequest;
import com.brakit.tournament.dto.request.UpdateTournamentRequestMapper;
import com.brakit.tournament.dto.response.TournamentResponse;
import com.brakit.tournament.dto.response.TournamentResponseMapper;
import com.brakit.tournament.model.Tournament;
import com.brakit.tournament.repository.TournamentRepository;
import com.brakit.tournament.service.exception.NotTournamentOwner;
import com.brakit.tournament.service.exception.TeamCountLimitBelowTeamCount;
import com.brakit.tournament.service.exception.TournamentNotFound;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped 
public class TournamentService {
	
	@Inject TournamentRepository tournamentRepository;
	@Inject TeamRepository teamRepository;

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
		// Locked so a team created concurrently can't push the count past a lowered limit
		var model = getOwnedForUpdate(id, userId)
				.merge(UpdateTournamentRequestMapper.toModel(id, request));
		var teamCount = teamRepository.countInTournament(id);
		if (teamCount > model.getTeamCountLimit()) {
			throw new TeamCountLimitBelowTeamCount(model.getTeamCountLimit(), teamCount);
		}
		var saved = tournamentRepository.save(model);
		return TournamentResponseMapper.toResponse(saved);
	}

	@Transactional
	public void deleteTournament(Long id, Long userId) {
		getOwned(id, userId);
		tournamentRepository.deleteById(id);
	}

	// A private tournament is reported as not found to anyone but its owner, so its existence isn't leaked
	public Tournament getVisible(Long id, Optional<Long> viewerId) {
		return requireVisible(id, tournamentRepository.getById(id), viewerId);
	}

	public Tournament getOwned(Long id, Long userId) {
		return requireOwned(id, getVisible(id, Optional.of(userId)), userId);
	}

	// Same as getOwned, but locks the tournament row until the current transaction ends
	public Tournament getOwnedForUpdate(Long id, Long userId) {
		var tournament = requireVisible(id, tournamentRepository.getByIdForUpdate(id), Optional.of(userId));
		return requireOwned(id, tournament, userId);
	}

	private Tournament requireVisible(Long id, Optional<Tournament> tournament, Optional<Long> viewerId) {
		return tournament
				.filter(t -> t.isVisibleTo(viewerId))
				.orElseThrow(() -> new TournamentNotFound(id));
	}

	private Tournament requireOwned(Long id, Tournament tournament, Long userId) {
		if (!tournament.isOwnedBy(userId)) {
			throw new NotTournamentOwner(id);
		}
		return tournament;
	}
}
