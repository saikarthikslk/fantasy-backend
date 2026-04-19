package com.security.demo.repo;

import com.security.demo.DBmodel.Acitivity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ActivityRepo extends JpaRepository<Acitivity,Integer> {
}
