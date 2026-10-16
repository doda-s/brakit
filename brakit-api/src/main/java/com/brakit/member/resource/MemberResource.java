package com.brakit.member.resource;

import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirementsSet;

import com.brakit.auth.CurrentUser;
import com.brakit.auth.OpenApiSecurity;
import com.brakit.member.dto.request.MemberRequest;
import com.brakit.member.service.MemberService;

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

@Path("/api/tournaments/{tournamentId}/teams/{teamId}/members")
public class MemberResource {

	@Inject MemberService memberService;
	@Inject CurrentUser currentUser;

	@GET()
	// Auth is optional here: Swagger UI only sends the token to operations that declare a security requirement
	@SecurityRequirementsSet(@SecurityRequirement(name = OpenApiSecurity.SCHEME))
	@SecurityRequirementsSet()
	public Response listMembers(@PathParam("tournamentId") Long tournamentId, @PathParam("teamId") Long teamId) {
		var response = memberService.listMembers(tournamentId, teamId, currentUser.id());
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@GET()
	// Auth is optional here: Swagger UI only sends the token to operations that declare a security requirement
	@SecurityRequirementsSet(@SecurityRequirement(name = OpenApiSecurity.SCHEME))
	@SecurityRequirementsSet()
	@Path("/{memberId}")
	public Response getMember(
		@PathParam("tournamentId") Long tournamentId,
		@PathParam("teamId") Long teamId,
		@PathParam("memberId") Long memberId
	) {
		var response = memberService.getMember(tournamentId, teamId, memberId, currentUser.id());
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@POST()
	@Authenticated
	public Response createMember(
		@PathParam("tournamentId") Long tournamentId,
		@PathParam("teamId") Long teamId,
		@Valid @NotNull MemberRequest request
	) {
		var response = memberService.createMember(tournamentId, teamId, request, currentUser.requireId());
		return Response.status(Response.Status.CREATED).entity(response).build();
	}

	@PUT()
	@Authenticated
	@Path("/{memberId}")
	public Response updateMember(
		@PathParam("tournamentId") Long tournamentId,
		@PathParam("teamId") Long teamId,
		@PathParam("memberId") Long memberId,
		@Valid @NotNull MemberRequest request
	) {
		var response = memberService.updateMember(tournamentId, teamId, memberId, request, currentUser.requireId());
		return Response.status(Response.Status.OK).entity(response).build();
	}

	@DELETE()
	@Authenticated
	@Path("/{memberId}")
	public Response deleteMember(
		@PathParam("tournamentId") Long tournamentId,
		@PathParam("teamId") Long teamId,
		@PathParam("memberId") Long memberId
	) {
		memberService.deleteMember(tournamentId, teamId, memberId, currentUser.requireId());
		return Response.status(Response.Status.NO_CONTENT).build();
	}
}
