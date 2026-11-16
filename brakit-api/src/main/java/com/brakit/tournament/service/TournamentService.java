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
import com.brakit.tournament.service.exception.NotTournamentCreator;
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
				.map(t -> TournamentResponseMapper.toResponse(t, viewerId));
	}

	public TournamentResponse getTournament(Long id, Optional<Long> viewerId) {
		return TournamentResponseMapper.toResponse(getVisible(id, viewerId), viewerId);
	}

	@Transactional 
	public TournamentResponse createTournament(CreateTournamentRequest request, Long ownerId) {
		var tournament = CreateTournamentRequestMapper.toModel(request, ownerId);
		var savedTournament = tournamentRepository.save(tournament);
		return TournamentResponseMapper.toResponse(savedTournament, Optional.of(ownerId));
	}

	@Transactional
	public TournamentResponse updateTournament(Long id, UpdateTournamentRequest request, Long userId) {
		// Locked so a team created concurrently can't push the count past a lowered limit
		var model = getManagedForUpdate(id, userId)
				.merge(UpdateTournamentRequestMapper.toModel(id, request));
		var teamCount = teamRepository.countInTournament(id);
		if (teamCount > model.getTeamCountLimit()) {
			throw new TeamCountLimitBelowTeamCount(model.getTeamCountLimit(), teamCount);
		}
		var saved = tournamentRepository.save(model);
		return TournamentResponseMapper.toResponse(saved, Optional.of(userId));
	}

	@Transactional
	public void deleteTournament(Long id, Long userId) {
		getCreated(id, userId);
		tournamentRepository.deleteById(id);
	}

	// A private tournament is reported as not found to anyone but its owners, so its existence isn't leaked
	public Tournament getVisible(Long id, Optional<Long> viewerId) {
		return requireVisible(id, tournamentRepository.getById(id), viewerId);
	}

	// For actions open to every owner: the creator and the co-owners
	public Tournament getManaged(Long id, Long userId) {
		return requireManaged(id, getVisible(id, Optional.of(userId)), userId);
	}

	// Same as getManaged, but locks the tournament row until the current transaction ends
	public Tournament getManagedForUpdate(Long id, Long userId) {
		return requireManaged(id, getVisibleForUpdate(id, userId), userId);
	}

	// For actions reserved to the creator: deleting the tournament and managing its co-owners
	public Tournament getCreated(Long id, Long userId) {
		return requireCreated(id, getVisible(id, Optional.of(userId)), userId);
	}

	// Same as getCreated, but locks the tournament row until the current transaction ends
	public Tournament getCreatedForUpdate(Long id, Long userId) {
		return requireCreated(id, getVisibleForUpdate(id, userId), userId);
	}

	private Tournament getVisibleForUpdate(Long id, Long userId) {
		return requireVisible(id, tournamentRepository.getByIdForUpdate(id), Optional.of(userId));
	}

	private Tournament requireVisible(Long id, Optional<Tournament> tournament, Optional<Long> viewerId) {
		return tournament
				.filter(t -> t.isVisibleTo(viewerId))
				.orElseThrow(() -> new TournamentNotFound(id));
	}

	private Tournament requireManaged(Long id, Tournament tournament, Long userId) {
		if (!tournament.isManagedBy(userId)) {
			throw new NotTournamentOwner(id);
		}
		return tournament;
	}

	private Tournament requireCreated(Long id, Tournament tournament, Long userId) {
		if (!tournament.isCreatedBy(userId)) {
			throw new NotTournamentCreator(id);
		}
		return tournament;
	}
}
