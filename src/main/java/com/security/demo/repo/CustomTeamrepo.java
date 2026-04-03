package com.security.demo.repo;

import com.security.demo.DBmodel.CustomTeamEntity;
import com.security.demo.model.DreamTeam;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomTeamrepo extends JpaRepository<CustomTeamEntity,Integer> {

    @Query(value = "delete from customteams where email =:emailid", nativeQuery = true)
    @Modifying
    @Transactional
    void deleteifexists(@Param("emailid") String email);

    @Query(value = "delete from customteams where email =:emailid and match_id = :matchid", nativeQuery = true)
    @Modifying
    @Transactional
    void deleteifexistsbym(@Param("emailid") String email , @Param("matchid") Integer mid);

    @Query(value = "select * from customteams where match_id = :id", nativeQuery = true)
    List<CustomTeamEntity> findbymatchid(@Param("id") Integer id);
    @Query(value = "select * from customteams where match_id = :id and email = :email", nativeQuery = true)
    CustomTeamEntity findbymatchidandemail(@Param("id") Integer id ,@Param("email") String email );
    CustomTeamEntity findByEmail(String email);
}
