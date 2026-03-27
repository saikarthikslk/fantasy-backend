package com.security.demo.repo;

import com.security.demo.DBmodel.TeamEntity;
import com.security.demo.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepo  extends JpaRepository<TeamEntity,Integer> {
}
