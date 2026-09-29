package com.brakit.exception;

import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

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
