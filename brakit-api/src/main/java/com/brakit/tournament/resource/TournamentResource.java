package com.brakit.tournament.resource;

import com.brakit.tournament.dto.request.CreateTournamentRequest;
import com.brakit.tournament.service.TournamentService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/tournaments")
public class TournamentResource {
	
	@Inject TournamentService tournamentService;

	@POST()
	public Response createTournament(@Valid CreateTournamentRequest request) {
		var response = tournamentService.createTournament(request);
		return Response.status(Response.Status.CREATED).entity(response).build();
	}

}
