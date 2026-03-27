package com.security.demo.repo;

import com.security.demo.DBmodel.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerRepo extends JpaRepository<PlayerEntity,Integer> {


    @Query("select p from PlayerEntity p where  p.team.teamId in (:ids)  ")
    public List<PlayerEntity> getPlayers(@Param("ids") List<Integer> list);
}
