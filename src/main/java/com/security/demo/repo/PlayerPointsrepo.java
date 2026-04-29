package com.security.demo.repo;

import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.DBmodel.PlayerPoints;
import com.security.demo.model.Pointdto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface PlayerPointsrepo extends JpaRepository<PlayerPoints,Integer> {
    @Query(value = "select * from player_stats where matchid=:id",nativeQuery = true)
    List<PlayerPoints> getpointsbymatchid(@Param("id") Integer id) ;
    @Query(value = "WITH rt AS (\n" +
            "  SELECT playerid, SUM(totalpoints) AS total\n" +
            "  FROM player_stats\n" +
            "  GROUP BY playerid\n" +
            ")\n" +
            "UPDATE player p\n" +
            "SET points = rt.total\n" +
            "FROM rt\n" +
            "WHERE p.id = rt.playerid;",nativeQuery = true)
    @Transactional
    @Modifying
    int updatepoints() ;

    @Query(value = "select  playerid ,sum(totalpoints) as points from   player_stats where matchid != :mid and  matchid in (select match_id  from match_info where state = 'Completed') group by playerid  \n" +
            "order by points desc",nativeQuery = true)
    List<Pointdto> getpoints(@Param("mid") Integer id) ;
}
