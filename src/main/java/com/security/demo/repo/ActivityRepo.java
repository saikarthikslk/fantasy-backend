package com.security.demo.repo;

import com.security.demo.DBmodel.Acitivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ActivityRepo extends JpaRepository<Acitivity,Integer> {

    @Query(value = "select count(*) from activity where track like  CONCAT('%', :matchid, '%') " , nativeQuery = true)
    public Integer getviewcount(@Param("matchid") Integer id );
}
