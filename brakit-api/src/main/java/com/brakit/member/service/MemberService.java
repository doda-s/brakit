package com.brakit.member.service;

import java.util.List;
import java.util.Optional;

import com.brakit.member.dto.request.MemberRequest;
import com.brakit.member.dto.request.MemberRequestMapper;
import com.brakit.member.dto.response.MemberResponse;
import com.brakit.member.dto.response.MemberResponseMapper;
import com.brakit.member.model.TeamMember;
import com.brakit.member.repository.TeamMemberRepository;
import com.brakit.member.service.exception.MemberNotFound;
import com.brakit.team.service.TeamService;
import com.brakit.tournament.service.TournamentService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MemberService {

	@Inject TeamMemberRepository memberRepository;
	@Inject TeamService teamService;
	@Inject TournamentService tournamentService;

	public List<MemberResponse> listMembers(Long tournamentId, Long teamId, Optional<Long> viewerId) {
		tournamentService.getVisible(tournamentId, viewerId);
		teamService.getInTournament(tournamentId, teamId);
		return memberRepository.getAllInTeam(teamId).stream()
				.map(MemberResponseMapper::toResponse)
				.toList();
	}

	public MemberResponse getMember(Long tournamentId, Long teamId, Long memberId, Optional<Long> viewerId) {
		tournamentService.getVisible(tournamentId, viewerId);
		teamService.getInTournament(tournamentId, teamId);
		return MemberResponseMapper.toResponse(getInTeam(teamId, memberId));
	}

	@Transactional
	public MemberResponse createMember(Long tournamentId, Long teamId, MemberRequest request, Long userId) {
		tournamentService.getOwned(tournamentId, userId);
		teamService.getInTournament(tournamentId, teamId);
		var member = MemberRequestMapper.toModel(request, teamId);
		return MemberResponseMapper.toResponse(memberRepository.save(member));
	}

	@Transactional
	public MemberResponse updateMember(Long tournamentId, Long teamId, Long memberId, MemberRequest request, Long userId) {
		tournamentService.getOwned(tournamentId, userId);
		teamService.getInTournament(tournamentId, teamId);
		var member = getInTeam(teamId, memberId).replace(request.name(), request.role());
		return MemberResponseMapper.toResponse(memberRepository.save(member));
	}

	@Transactional
	public void deleteMember(Long tournamentId, Long teamId, Long memberId, Long userId) {
		tournamentService.getOwned(tournamentId, userId);
		teamService.getInTournament(tournamentId, teamId);
		if (!memberRepository.deleteByIdInTeam(memberId, teamId)) {
			throw new MemberNotFound(memberId);
		}
	}

	// A member id from another team is reported as not found
	private TeamMember getInTeam(Long teamId, Long memberId) {
		return memberRepository.getByIdInTeam(memberId, teamId)
				.orElseThrow(() -> new MemberNotFound(memberId));
	}
}
