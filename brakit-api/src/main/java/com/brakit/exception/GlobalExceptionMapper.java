package com.brakit.exception;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.brakit.auth.service.exception.EmailAlreadyRegistered;
import com.brakit.auth.service.exception.InvalidCredentials;
import com.brakit.auth.service.exception.UserNotFound;
import com.brakit.member.service.exception.MemberNotFound;
import com.brakit.team.service.exception.TeamNameAlreadyTaken;
import com.brakit.team.service.exception.TeamNotFound;
import com.brakit.team.service.exception.TournamentFull;
import com.brakit.tournament.service.exception.NotTournamentOwner;
import com.brakit.tournament.service.exception.TeamCountLimitBelowTeamCount;
import com.brakit.tournament.service.exception.TournamentNotFound;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;

public class GlobalExceptionMapper {
	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapTournamentNotFound(TournamentNotFound e) {
		return RestResponse.status(
			RestResponse.Status.NOT_FOUND,
			new ErrorResponse(404, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapNotTournamentOwner(NotTournamentOwner e) {
		return RestResponse.status(
			RestResponse.Status.FORBIDDEN,
			new ErrorResponse(403, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapTeamCountLimitBelowTeamCount(TeamCountLimitBelowTeamCount e) {
		return RestResponse.status(
			RestResponse.Status.CONFLICT,
			new ErrorResponse(409, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapTeamNotFound(TeamNotFound e) {
		return RestResponse.status(
			RestResponse.Status.NOT_FOUND,
			new ErrorResponse(404, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapMemberNotFound(MemberNotFound e) {
		return RestResponse.status(
			RestResponse.Status.NOT_FOUND,
			new ErrorResponse(404, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapTeamNameAlreadyTaken(TeamNameAlreadyTaken e) {
		return RestResponse.status(
			RestResponse.Status.CONFLICT,
			new ErrorResponse(409, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapTournamentFull(TournamentFull e) {
		return RestResponse.status(
			RestResponse.Status.CONFLICT,
			new ErrorResponse(409, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapUserNotFound(UserNotFound e) {
		return RestResponse.status(
			RestResponse.Status.NOT_FOUND,
			new ErrorResponse(404, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapEmailAlreadyRegistered(EmailAlreadyRegistered e) {
		return RestResponse.status(
			RestResponse.Status.CONFLICT,
			new ErrorResponse(409, e.getMessage())
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapInvalidCredentials(InvalidCredentials e) {
		return RestResponse.status(
			RestResponse.Status.UNAUTHORIZED,
			new ErrorResponse(401, e.getMessage())
		);
	}

	// Unique constraint hit by a concurrent insert that slipped past the service's existence check
	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapDatabaseConstraintViolation(org.hibernate.exception.ConstraintViolationException e) {
		if (e.getKind() != org.hibernate.exception.ConstraintViolationException.ConstraintKind.UNIQUE) {
			return RestResponse.status(
				RestResponse.Status.INTERNAL_SERVER_ERROR,
				new ErrorResponse(500, "Internal server error.")
			);
		}
		return RestResponse.status(
			RestResponse.Status.CONFLICT,
			new ErrorResponse(409, "Resource conflicts with an existing one.")
		);
	}

	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapConstraintViolation(ConstraintViolationException e) {
		var message = e.getConstraintViolations().stream()
			.map(GlobalExceptionMapper::describe)
			.sorted()
			.collect(Collectors.joining("; "));
		return RestResponse.status(
			RestResponse.Status.BAD_REQUEST,
			new ErrorResponse(400, message)
		);
	}

	// Reports the field path without the method and parameter prefix, keeping list indexes,
	// e.g. "createTeam.request.members[2].name" -> "members[2].name", "listTournaments.page" -> "page"
	private static String describe(ConstraintViolation<?> violation) {
		var nodes = new ArrayList<Path.Node>();
		violation.getPropertyPath().forEach(nodes::add);
		var field = new StringBuilder();
		for (int i = 0; i < nodes.size(); i++) {
			var node = nodes.get(i);
			var isLast = i == nodes.size() - 1;
			if (node.getKind() == ElementKind.METHOD || (node.getKind() == ElementKind.PARAMETER && !isLast)) {
				continue;
			}
			// The index belongs to the enclosing list, e.g. the [2] in members[2].name
			if (node.isInIterable() && node.getIndex() != null) {
				field.append('[').append(node.getIndex()).append(']');
			}
			// Container element nodes are named like "<list element>" and add nothing to the path
			if (node.getKind() == ElementKind.CONTAINER_ELEMENT || node.getName() == null) {
				continue;
			}
			if (!field.isEmpty()) {
				field.append('.');
			}
			field.append(node.getName());
		}
		return field + ": " + violation.getMessage();
	}
}
