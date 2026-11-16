package com.brakit.tournament.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Tournament {
	private Long id;
	private String name;
	private String description;
	private TournamentVisibility visibility;
	private int teamCountLimit;
	private Long ownerId;
	private Set<Long> coOwnerIds;

	public Tournament(
		Long id,
		String name,
		String description,
		TournamentVisibility visibility,
		int teamCountLimit,
		Long ownerId,
		Set<Long> coOwnerIds
	) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.visibility = visibility;
		this.teamCountLimit = teamCountLimit;
		this.ownerId = ownerId;
		this.coOwnerIds = new HashSet<>(coOwnerIds);
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

	public Set<Long> getCoOwnerIds() {
		return Set.copyOf(coOwnerIds);
	}

	public static Tournament create(
		String name,
		String description,
		TournamentVisibility visibility,
		int teamCountLimit,
		Long ownerId
	) {
		return new Tournament(null, name, description, visibility, teamCountLimit, ownerId, Set.of());
	}

	public boolean isCreatedBy(Long userId) {
		return ownerId.equals(userId);
	}

	// True for the creator and the co-owners
	public boolean isManagedBy(Long userId) {
		return roleOf(userId).isPresent();
	}

	public Optional<TournamentRole> roleOf(Long userId) {
		if (isCreatedBy(userId)) {
			return Optional.of(TournamentRole.CREATOR);
		}
		if (coOwnerIds.contains(userId)) {
			return Optional.of(TournamentRole.CO_OWNER);
		}
		return Optional.empty();
	}

	public boolean isVisibleTo(Optional<Long> viewerId) {
		return visibility == TournamentVisibility.PUBLIC || viewerId.filter(this::isManagedBy).isPresent();
	}

	// False when the user already manages the tournament, as creator or co-owner
	public boolean addCoOwner(Long userId) {
		return !isCreatedBy(userId) && coOwnerIds.add(userId);
	}

	// False when the user isn't a co-owner
	public boolean removeCoOwner(Long userId) {
		return coOwnerIds.remove(userId);
	}

	public Tournament merge(Tournament t) {
		this.name = t.name != null? t.name : this.name;
		this.description = t.description != null? t.description : this.description;
		this.visibility = t.visibility != null? t.visibility : this.visibility;
		this.teamCountLimit = t.teamCountLimit > 0? t.teamCountLimit : this.teamCountLimit;
		return this;
	}
}
