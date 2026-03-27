package com.security.demo.repo;

import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.DBmodel.VenueEntity;
import com.security.demo.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Venuerepo extends JpaRepository<VenueEntity,Integer> {
}
