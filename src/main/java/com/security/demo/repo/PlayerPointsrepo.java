package com.security.demo.repo;

import com.security.demo.DBmodel.PlayerEntity;
import com.security.demo.DBmodel.PlayerPoints;
import com.security.demo.model.Playerstat;
import com.security.demo.model.Pointdto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Query(value =
            "SELECT team1, team2, ballsbowled, playerid, totalpoints as score, ballplayed, wickets, eco, scoregiven ,pos " +
                    "FROM ( " +
                    "    SELECT " +
                    "        ROW_NUMBER() OVER (PARTITION BY p.playerid ORDER BY m.start_date DESC) AS pos, " +
                    "        p.playerid, " +
                    "        t1.team_s_name AS team1, " +
                    "        t2.team_s_name AS team2, " +
                    "        p.totalpoints, " +
                    "        p.ballplayed, " +
                    "        p.wickets, " +
                    "        p.ballsbowled, " +
                    "        p.eco, " +
                    "        p.scoregiven " +
                    "    FROM player_stats p " +
                    "    JOIN match_info m ON p.matchid = m.match_id " +
                    "    JOIN team t1 ON m.team1_id = t1.team_id " +
                    "    JOIN team t2 ON m.team2_id = t2.team_id " +
                    "    WHERE m.state = 'Completed' AND p.playerid IN (:ids) " +
                    ") t " +
                    "WHERE pos <= 5",
            nativeQuery = true)
    List<Playerstat> getPoints(@Param("ids") List<String> ids);
}
