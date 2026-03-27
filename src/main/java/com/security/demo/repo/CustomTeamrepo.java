package com.security.demo.repo;

import com.security.demo.DBmodel.CustomTeamEntity;
import com.security.demo.model.DreamTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomTeamrepo extends JpaRepository<CustomTeamEntity,Integer> {

    @Query(value = "delete from customteams where email =:emailid", nativeQuery = true)
    @Modifying
    void deleteifexists(@Param("emailid") String email);

    CustomTeamEntity findByEmail(String email);
}
