package com.brakit.tournament.model;

public class Tournament {
	private Long id;
	private String name;
	private String description;
	private TournamentVisibility visibility;
	private int teamCountLimit;

	public Tournament(
		Long id,
		String name,
		String description,
		TournamentVisibility visibility,
		int teamCountLimit
	) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.visibility = visibility;
		this.teamCountLimit = teamCountLimit;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TournamentVisibility getVisibility() {
		return visibility;
	}

	public void setVisibility(TournamentVisibility visibility) {
		this.visibility = visibility;
	}

	public int getTeamCountLimit() {
		return teamCountLimit;
	}

	public void setTeamCountLimit(int teamCountLimit) {
		this.teamCountLimit = teamCountLimit;
	}

	public static Tournament create(
		String name,
		String description,
		TournamentVisibility visibility,
		int teamCountLimit
	) {
		return new Tournament(null, name, description, visibility, teamCountLimit);
	}
}
