package com.brakit.team.service;

import java.util.List;
import java.util.Optional;

import com.brakit.member.dto.request.MemberRequestMapper;
import com.brakit.member.dto.response.MemberResponseMapper;
import com.brakit.member.repository.TeamMemberRepository;
import com.brakit.team.dto.request.CreateTeamRequest;
import com.brakit.team.dto.request.CreateTeamRequestMapper;
import com.brakit.team.dto.request.UpdateTeamRequest;
import com.brakit.team.dto.response.TeamResponse;
import com.brakit.team.dto.response.TeamResponseMapper;
import com.brakit.team.dto.response.TeamWithMembersResponse;
import com.brakit.team.model.Team;
import com.brakit.team.repository.TeamRepository;
import com.brakit.team.service.exception.TeamNameAlreadyTaken;
import com.brakit.team.service.exception.TeamNotFound;
import com.brakit.team.service.exception.TournamentFull;
import com.brakit.tournament.service.TournamentService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TeamService {

	@Inject TeamRepository teamRepository;
	@Inject TeamMemberRepository memberRepository;
	@Inject TournamentService tournamentService;

	public List<TeamResponse> listTeams(Long tournamentId, Optional<Long> viewerId) {
		tournamentService.getVisible(tournamentId, viewerId);
		return teamRepository.getAllInTournament(tournamentId).stream()
				.map(TeamResponseMapper::toResponse)
				.toList();
	}

	public TeamResponse getTeam(Long tournamentId, Long teamId, Optional<Long> viewerId) {
		tournamentService.getVisible(tournamentId, viewerId);
		return TeamResponseMapper.toResponse(getInTournament(tournamentId, teamId));
	}

	@Transactional
	public TeamWithMembersResponse createTeam(Long tournamentId, CreateTeamRequest request, Long userId) {
		// Locking the tournament serializes concurrent creations, so the count check below can't be raced past
		var tournament = tournamentService.getOwnedForUpdate(tournamentId, userId);
		if (teamRepository.countInTournament(tournamentId) >= tournament.getTeamCountLimit()) {
			throw new TournamentFull(tournament.getTeamCountLimit());
		}
		var team = CreateTeamRequestMapper.toModel(request, tournamentId);
		requireUniqueName(team, tournamentId);
		var savedTeam = teamRepository.save(team);
		var members = request.members().stream()
				.map(m -> memberRepository.save(MemberRequestMapper.toModel(m, savedTeam.getId())))
				.map(MemberResponseMapper::toResponse)
				.toList();
		return TeamResponseMapper.toResponse(savedTeam, members);
	}

	@Transactional
	public TeamResponse updateTeam(Long tournamentId, Long teamId, UpdateTeamRequest request, Long userId) {
		tournamentService.getOwned(tournamentId, userId);
		var team = getInTournament(tournamentId, teamId).rename(request.name());
		requireUniqueName(team, tournamentId);
		return TeamResponseMapper.toResponse(teamRepository.save(team));
	}

	@Transactional
	public void deleteTeam(Long tournamentId, Long teamId, Long userId) {
		tournamentService.getOwned(tournamentId, userId);
		if (!teamRepository.deleteByIdInTournament(teamId, tournamentId)) {
			throw new TeamNotFound(teamId);
		}
	}

	// A team id from another tournament is reported as not found
	public Team getInTournament(Long tournamentId, Long teamId) {
		return teamRepository.getByIdInTournament(teamId, tournamentId)
				.orElseThrow(() -> new TeamNotFound(teamId));
	}

	private void requireUniqueName(Team team, Long tournamentId) {
		if (teamRepository.existsByNameInTournament(team.getName(), tournamentId, team.getId())) {
			throw new TeamNameAlreadyTaken(team.getName());
		}
	}
}
