package com.security.demo.repo;

import com.security.demo.DBmodel.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Userrepo extends JpaRepository<User,Integer> {
    User findByEmail(String email);
}
