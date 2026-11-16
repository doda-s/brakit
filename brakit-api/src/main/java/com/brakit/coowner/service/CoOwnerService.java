package com.brakit.coowner.service;

import java.util.List;

import com.brakit.auth.dto.response.UserResponse;
import com.brakit.auth.dto.response.UserResponseMapper;
import com.brakit.auth.service.exception.UserNotFound;
import com.brakit.coowner.dto.request.AddCoOwnerRequest;
import com.brakit.coowner.service.exception.AlreadyTournamentOwner;
import com.brakit.coowner.service.exception.CoOwnerNotFound;
import com.brakit.tournament.repository.TournamentRepository;
import com.brakit.tournament.service.TournamentService;
import com.brakit.user.model.User;
import com.brakit.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CoOwnerService {

	@Inject TournamentService tournamentService;
	@Inject TournamentRepository tournamentRepository;
	@Inject UserRepository userRepository;

	public List<UserResponse> listCoOwners(Long tournamentId, Long userId) {
		var tournament = tournamentService.getManaged(tournamentId, userId);
		return userRepository.getAllByIds(tournament.getCoOwnerIds()).stream()
				.map(UserResponseMapper::toResponse)
				.toList();
	}

	@Transactional
	public UserResponse addCoOwner(Long tournamentId, AddCoOwnerRequest request, Long userId) {
		// Locked so concurrent changes to the co-owners, or to the tournament itself, can't overwrite each other
		var tournament = tournamentService.getCreatedForUpdate(tournamentId, userId);
		var email = User.normalizeEmail(request.email());
		var user = userRepository.getByEmail(email)
				.orElseThrow(() -> new UserNotFound(email));
		if (!tournament.addCoOwner(user.getId())) {
			throw new AlreadyTournamentOwner(email);
		}
		tournamentRepository.save(tournament);
		return UserResponseMapper.toResponse(user);
	}

	@Transactional
	public void removeCoOwner(Long tournamentId, Long coOwnerId, Long userId) {
		var tournament = tournamentService.getCreatedForUpdate(tournamentId, userId);
		if (!tournament.removeCoOwner(coOwnerId)) {
			throw new CoOwnerNotFound(coOwnerId);
		}
		tournamentRepository.save(tournament);
	}
}
