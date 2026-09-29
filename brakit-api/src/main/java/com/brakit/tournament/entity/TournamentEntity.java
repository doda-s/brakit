package com.brakit.tournament.entity;

import com.brakit.tournament.model.TournamentVisibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tournament")
public class TournamentEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Column(nullable = false) private String name;
	@Column(nullable = false) private String description;
	@Column(nullable = false) @Enumerated(EnumType.STRING) private TournamentVisibility visibility;
	@Column(nullable = false) private int teamCountLimit;

	protected TournamentEntity() {}

	public TournamentEntity(
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
}
