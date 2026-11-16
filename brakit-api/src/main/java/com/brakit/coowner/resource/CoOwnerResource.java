package com.brakit.coowner.resource;

import com.brakit.auth.CurrentUser;
import com.brakit.coowner.dto.request.AddCoOwnerRequest;
import com.brakit.coowner.service.CoOwnerService;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

// Every owner can see the co-owners; only the creator can add or remove them
@Path("/api/tournaments/{tournamentId}/co-owners")
@Authenticated
public class CoOwnerResource {

	@Inject CoOwnerService coOwnerService;
	@Inject CurrentUser currentUser;

	@GET()
	public Response listCoOwners(@PathParam("tournamentId") Long tournamentId) {
		var response = coOwnerService.listCoOwners(tournamentId, currentUser.requireId());
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@POST()
	public Response addCoOwner(@PathParam("tournamentId") Long tournamentId, @Valid @NotNull AddCoOwnerRequest request) {
		var response = coOwnerService.addCoOwner(tournamentId, request, currentUser.requireId());
		return Response.status(Response.Status.CREATED).entity(response).build();
	}

	@DELETE()
	@Path("/{userId}")
	public Response removeCoOwner(@PathParam("tournamentId") Long tournamentId, @PathParam("userId") Long userId) {
		coOwnerService.removeCoOwner(tournamentId, userId, currentUser.requireId());
		return Response.status(Response.Status.NO_CONTENT).build();
	}
}
