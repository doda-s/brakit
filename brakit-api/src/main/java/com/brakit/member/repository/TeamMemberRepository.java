package com.brakit.member.repository;

import java.util.List;
import java.util.Optional;

import com.brakit.member.entity.TeamMemberEntity;
import com.brakit.member.model.TeamMember;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TeamMemberRepository implements PanacheRepository<TeamMemberEntity> {

	public TeamMember save(TeamMember model) {
		TeamMemberEntity entity = TeamMemberMapper.toEntity(model);
		var savedEntity = this.getEntityManager().merge(entity);
		return TeamMemberMapper.toModel(savedEntity);
	}

	public Optional<TeamMember> getByIdInTeam(Long id, Long teamId) {
		var entity = this.find("id = ?1 and teamId = ?2", id, teamId).firstResultOptional();
		return entity.map(TeamMemberMapper::toModel);
	}

	public List<TeamMember> getAllInTeam(Long teamId) {
		return this.find("teamId", Sort.by("id"), teamId).list().stream()
				.map(TeamMemberMapper::toModel)
				.toList();
	}

	public boolean deleteByIdInTeam(Long id, Long teamId) {
		return this.delete("id = ?1 and teamId = ?2", id, teamId) > 0;
	}
}
