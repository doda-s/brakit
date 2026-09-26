package com.brakit.tournament.repository;

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
}
