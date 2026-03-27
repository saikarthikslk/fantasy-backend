package com.security.demo.service;

import com.security.demo.DBmodel.User;
import com.security.demo.repo.Userrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;

@Service
public class UserService {

    @Autowired
    private Userrepo userrepo;

    public User createifnotExists(String email ,String name) {
      User user =    userrepo.findByEmail(email);
      if(user == null) {
          User new_user = new User();
          new_user.setName(name);
          new_user.setEmail(email);
          new_user.setCreated_at(Timestamp.from(Instant.now()));
          userrepo.save(new_user);
          return user;
      }
      return user;
    }

}
