package com.brakit.team.resource;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSet;

import com.brakit.auth.CurrentUser;
import com.brakit.auth.OpenApiSecurity;
import com.brakit.team.dto.request.CreateTeamRequest;
import com.brakit.team.dto.request.UpdateTeamRequest;
import com.brakit.team.service.TeamService;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/api/tournaments/{tournamentId}/teams")
public class TeamResource {

	@Inject TeamService teamService;
	@Inject CurrentUser currentUser;

	@GET()
	// Auth is optional here: Swagger UI only sends the token to operations that declare a security requirement
	@SecurityRequirementsSet(@SecurityRequirement(name = OpenApiSecurity.SCHEME))
	@SecurityRequirementsSet()
	public Response listTeams(@PathParam("tournamentId") Long tournamentId) {
		var response = teamService.listTeams(tournamentId, currentUser.id());
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@GET()
	// Auth is optional here: Swagger UI only sends the token to operations that declare a security requirement
	@SecurityRequirementsSet(@SecurityRequirement(name = OpenApiSecurity.SCHEME))
	@SecurityRequirementsSet()
	@Path("/{teamId}")
	public Response getTeam(@PathParam("tournamentId") Long tournamentId, @PathParam("teamId") Long teamId) {
		var response = teamService.getTeam(tournamentId, teamId, currentUser.id());
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@POST()
	@Authenticated
	public Response createTeam(@PathParam("tournamentId") Long tournamentId, @Valid @NotNull CreateTeamRequest request) {
		var response = teamService.createTeam(tournamentId, request, currentUser.requireId());
		return Response.status(Response.Status.CREATED).entity(response).build();
	}

	@PUT()
	@Authenticated
	@Path("/{teamId}")
	public Response updateTeam(
		@PathParam("tournamentId") Long tournamentId,
		@PathParam("teamId") Long teamId,
		@Valid @NotNull UpdateTeamRequest request
	) {
		var response = teamService.updateTeam(tournamentId, teamId, request, currentUser.requireId());
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@DELETE()
	@Authenticated
	@Path("/{teamId}")
	public Response deleteTeam(@PathParam("tournamentId") Long tournamentId, @PathParam("teamId") Long teamId) {
		teamService.deleteTeam(tournamentId, teamId, currentUser.requireId());
		return Response.status(Response.Status.NO_CONTENT).build();
	}
}
