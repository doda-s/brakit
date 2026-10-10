package com.brakit.team.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.brakit.team.entity.TeamEntity;
import com.brakit.team.model.Team;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TeamRepository implements PanacheRepository<TeamEntity> {

	public Team save(Team model) {
		TeamEntity entity = TeamMapper.toEntity(model);
		var savedEntity = this.getEntityManager().merge(entity);
		return TeamMapper.toModel(savedEntity);
	}

	public Optional<Team> getByIdInTournament(Long id, Long tournamentId) {
		var entity = this.find("id = ?1 and tournamentId = ?2", id, tournamentId).firstResultOptional();
		return entity.map(TeamMapper::toModel);
	}

	public List<Team> getAllInTournament(Long tournamentId) {
		return this.find("tournamentId", Sort.by("id"), tournamentId).list().stream()
				.map(TeamMapper::toModel)
				.toList();
	}

	public long countInTournament(Long tournamentId) {
		return this.count("tournamentId", tournamentId);
	}

	// Case-insensitive, matching the team_tournament_id_name_key index; excludeId lets a team keep its own name
	public boolean existsByNameInTournament(String name, Long tournamentId, Long excludeId) {
		var query = "tournamentId = :tournamentId and lower(name) = lower(:name)";
		if (excludeId == null) {
			return this.count(query, Map.of("tournamentId", tournamentId, "name", name)) > 0;
		}
		return this.count(query + " and id <> :excludeId",
				Map.of("tournamentId", tournamentId, "name", name, "excludeId", excludeId)) > 0;
	}

	public boolean deleteByIdInTournament(Long id, Long tournamentId) {
		return this.delete("id = ?1 and tournamentId = ?2", id, tournamentId) > 0;
	}
}
