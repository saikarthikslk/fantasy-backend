package com.security.demo.repo;

import com.security.demo.DBmodel.MatchState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

public interface MatchStaterepo  extends JpaRepository<MatchState,Integer> {
    @Query(value = "select * from matchstatus where matchid = :matchid" , nativeQuery = true)
    MatchState getstate(@Param("matchid") Integer matchid);
}
