package com.brakit.tournament.repository;

import java.util.Optional;

import com.brakit.tournament.entity.TournamentEntity;
import com.brakit.tournament.model.Tournament;
import com.brakit.tournament.service.TournamentMapper;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped 
public class TournamentRepository implements PanacheRepository<TournamentEntity> {

	@Transactional
	public Tournament save(Tournament model) {
		TournamentEntity entity = TournamentMapper.toEntity(model);
		var savedEntity = this.getEntityManager().merge(entity);
		return TournamentMapper.toModel(savedEntity);
	}

	@Transactional
	public Optional<Tournament> getById(Long id) {
		var entity = this.findByIdOptional(id);
		return entity.map(TournamentMapper::toModel);
	}
}
