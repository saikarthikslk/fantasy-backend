package com.security.demo.config;

import com.security.demo.repo.Userrepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class Userdetailsservice implements UserDetailsService {
    @Autowired
    Userrepo userrepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.security.demo.DBmodel.User user=  userrepo.findByEmail(username);
        if(user == null) {
            throw new UsernameNotFoundException("email not found for the " + username );
        }

       return   new User(username , "","");
    }
}
