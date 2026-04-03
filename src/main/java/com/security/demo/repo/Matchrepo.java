package com.security.demo.repo;

import com.security.demo.DBmodel.MatchInfoEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Matchrepo extends JpaRepository<MatchInfoEntity,Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from venue where id = :id" , nativeQuery = true)
    void deletebyvid(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query(value = "delete from match_info where match_id = :id" , nativeQuery = true)
    void deletebymid(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query(value = "delete from team where team_id = :id" , nativeQuery = true)
    void deletebytid(@Param("id") Integer id);


    @Query(value = "select * from match_info  where state  in (:ids) " , nativeQuery = true)
    List<MatchInfoEntity> fetchmatchescompletedorlive(@Param("ids") List<String> ids);
}
