package com.security.demo.repo;

import com.security.demo.DBmodel.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Userrepo extends JpaRepository<User,Integer> {
    User findByEmail(String email);
    @Query(value = "select num from users where num is not null" , nativeQuery = true)
    List<String> getnums();
}
