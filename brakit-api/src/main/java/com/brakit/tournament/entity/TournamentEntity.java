package com.brakit.tournament.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;

import com.brakit.tournament.model.TournamentVisibility;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "tournament")
public class TournamentEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(nullable = false) private String name;
	@Column(nullable = false) private String description;
	@Column(nullable = false) @Enumerated(EnumType.STRING) private TournamentVisibility visibility;
	@Column(nullable = false) private int teamCountLimit;
	@Column(nullable = false) private Long ownerId;
	// Batched so a page of tournaments loads its co-owners in one query (100 is the largest page size)
	@ElementCollection
	@CollectionTable(name = "tournament_co_owner", joinColumns = @JoinColumn(name = "tournament_id"))
	@Column(name = "user_id")
	@BatchSize(size = 100)
	private Set<Long> coOwnerIds = new HashSet<>();

	protected TournamentEntity() {}

	public TournamentEntity(
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
		return coOwnerIds;
	}

	public void setCoOwnerIds(Set<Long> coOwnerIds) {
		this.coOwnerIds = coOwnerIds;
	}
}
