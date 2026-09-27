package com.brakit.exception;

import java.util.concurrent.CompletionException;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.resteasy.reactive.server.UnwrapException;

import com.brakit.tournament.service.exception.TournamentNotFound;

@UnwrapException({CompletionException.class, RuntimeException.class})
public class GlobalExceptionMapper {
	@ServerExceptionMapper
    public RestResponse<ErrorResponse> mapTournamentNotFound(TournamentNotFound e) {
        return RestResponse.status(
            RestResponse.Status.NOT_FOUND, 
            new ErrorResponse(404, "Tournament not found: " + e.getMessage())
        );
    }
}
