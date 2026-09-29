package com.brakit.tournament.repository;

import java.util.Optional;

import com.brakit.tournament.entity.TournamentEntity;
import com.brakit.tournament.model.Tournament;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class TournamentRepository implements PanacheRepository<TournamentEntity> {

	public Tournament save(Tournament model) {
		TournamentEntity entity = TournamentMapper.toEntity(model);
		var savedEntity = this.getEntityManager().merge(entity);
		return TournamentMapper.toModel(savedEntity);
	}

	public Optional<Tournament> getById(Long id) {
		var entity = this.findByIdOptional(id);
		return entity.map(TournamentMapper::toModel);
	}
}
