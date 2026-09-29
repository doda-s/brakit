package com.brakit.tournament.resource;

import com.brakit.tournament.dto.request.CreateTournamentRequest;
import com.brakit.tournament.dto.request.UpdateTournamentRequest;
import com.brakit.tournament.service.TournamentService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/api/tournaments")
public class TournamentResource {
	
	@Inject TournamentService tournamentService;

	@GET()
	public Response listTournaments(
		@QueryParam("page") @DefaultValue("0") @Min(0) int page,
		@QueryParam("size") @DefaultValue("20") @Min(1) @Max(100) int size
	) {
		var response = tournamentService.listTournaments(page, size);
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@GET()
	@Path("/{id}")
	public Response getTournament(@PathParam("id") Long id) {
		var response = tournamentService.getTournament(id);
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@POST()
	public Response createTournament(@Valid @NotNull CreateTournamentRequest request) {
		var response = tournamentService.createTournament(request);
		return Response.status(Response.Status.CREATED).entity(response).build();
	}

	@PUT()
	@Path("/{id}")
	public Response updateTournament(@PathParam("id") Long id, @Valid @NotNull UpdateTournamentRequest request) {
		var response = tournamentService.updateTournament(id, request);
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@DELETE()
	@Path("/{id}")
	public Response deleteTournament(@PathParam("id") Long id) {
		tournamentService.deleteTournament(id);
		return Response.status(Response.Status.NO_CONTENT).build();
	}
}
