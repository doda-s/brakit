package com.brakit.tournament.repository;

import java.util.Optional;

import com.brakit.pagination.Page;
import com.brakit.tournament.entity.TournamentEntity;
import com.brakit.tournament.model.Tournament;
import com.brakit.tournament.model.TournamentVisibility;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
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

	// Public tournaments plus, for an authenticated viewer, their own private ones
	public Page<Tournament> getVisiblePage(Optional<Long> viewerId, int page, int size) {
		var sort = Sort.by("id");
		var query = viewerId
				.map(id -> this.find("visibility = :public or ownerId = :viewer", sort,
						Parameters.with("public", TournamentVisibility.PUBLIC).and("viewer", id)))
				.orElseGet(() -> this.find("visibility", sort, TournamentVisibility.PUBLIC))
				.page(page, size);
		var content = query.list().stream()
				.map(TournamentMapper::toModel)
				.toList();
		return new Page<>(content, page, size, query.count(), query.pageCount());
	}
}
