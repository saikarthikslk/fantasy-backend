package com.security.demo.repo;

import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.DBmodel.PlayerPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerPointsrepo extends JpaRepository<PlayerPoints,Integer> {
    @Query(value = "select * from player_stats where matchid=:id",nativeQuery = true)
    List<PlayerPoints> getpointsbymatchid(@Param("id") Integer id) ;
}
