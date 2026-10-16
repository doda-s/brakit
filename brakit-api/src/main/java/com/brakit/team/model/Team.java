package com.brakit.team.model;

public class Team {
	private Long id;
	private Long tournamentId;
	private String name;

	public Team(
		Long id,
		Long tournamentId,
		String name
	) {
		this.id = id;
		this.tournamentId = tournamentId;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTournamentId() {
		return tournamentId;
	}

	public void setTournamentId(Long tournamentId) {
		this.tournamentId = tournamentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Team create(Long tournamentId, String name) {
		return new Team(null, tournamentId, normalizeName(name));
	}

	public Team rename(String name) {
		this.name = normalizeName(name);
		return this;
	}

	public static String normalizeName(String name) {
		return name.trim();
	}
}
