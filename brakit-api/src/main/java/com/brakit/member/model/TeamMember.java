package com.brakit.member.model;

public class TeamMember {
	private Long id;
	private Long teamId;
	private String name;
	private String role;

	public TeamMember(
		Long id,
		Long teamId,
		String name,
		String role
	) {
		this.id = id;
		this.teamId = teamId;
		this.name = name;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public static TeamMember create(Long teamId, String name, String role) {
		return new TeamMember(null, teamId, name.trim(), normalizeRole(role));
	}

	// Replaces both fields: a missing role clears it
	public TeamMember replace(String name, String role) {
		this.name = name.trim();
		this.role = normalizeRole(role);
		return this;
	}

	private static String normalizeRole(String role) {
		return role != null ? role.trim() : null;
	}
}
