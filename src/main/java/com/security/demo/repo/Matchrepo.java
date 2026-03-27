package com.security.demo.repo;

import com.security.demo.DBmodel.MatchInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Matchrepo extends JpaRepository<MatchInfoEntity,Integer> {
}
