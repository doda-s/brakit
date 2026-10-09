package com.brakit.tournament.model;

import java.util.Optional;

public class Tournament {
	private Long id;
	private String name;
	private String description;
	private TournamentVisibility visibility;
	private int teamCountLimit;
	private Long ownerId;

	public Tournament(
		Long id,
		String name,
		String description,
		TournamentVisibility visibility,
		int teamCountLimit,
		Long ownerId
	) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.visibility = visibility;
		this.teamCountLimit = teamCountLimit;
		this.ownerId = ownerId;
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

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public static Tournament create(
		String name,
		String description,
		TournamentVisibility visibility,
		int teamCountLimit,
		Long ownerId
	) {
		return new Tournament(null, name, description, visibility, teamCountLimit, ownerId);
	}

	public boolean isOwnedBy(Long userId) {
		return ownerId.equals(userId);
	}

	public boolean isVisibleTo(Optional<Long> viewerId) {
		return visibility == TournamentVisibility.PUBLIC || viewerId.filter(this::isOwnedBy).isPresent();
	}

	public Tournament merge(Tournament t) {
		this.name = t.name != null? t.name : this.name;
		this.description = t.description != null? t.description : this.description;
		this.visibility = t.visibility != null? t.visibility : this.visibility;
		this.teamCountLimit = t.teamCountLimit > 0? t.teamCountLimit : this.teamCountLimit;
		return this;
	}
}
