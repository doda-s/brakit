package com.brakit.exception;

import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.brakit.auth.service.exception.EmailAlreadyRegistered;
import com.brakit.auth.service.exception.InvalidCredentials;
import com.brakit.auth.service.exception.UserNotFound;
import com.brakit.tournament.service.exception.TournamentNotFound;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

public class GlobalExceptionMapper {
	@ServerExceptionMapper
	public RestResponse<ErrorResponse> mapTournamentNotFound(TournamentNotFound e) {
		return RestResponse.status(
			RestResponse.Status.NOT_FOUND,
			new ErrorResponse(404, e.getMessage())
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

	// Drops the "method.argN." prefix so only the field path is reported, e.g. "request.name" -> "name".
	private static String describe(ConstraintViolation<?> violation) {
		var path = violation.getPropertyPath().toString();
		var field = path.substring(path.lastIndexOf('.') + 1).replaceAll("<.*>", "");
		return field + ": " + violation.getMessage();
	}
}
