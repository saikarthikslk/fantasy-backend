package com.security.demo.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class User implements org.springframework.security.core.userdetails.UserDetails {
    private String user;
    private String pass;
    private String auth;
    public User(String user, String pass , String auth) {
        this.user = user;
        this.pass=pass;
        this.auth = auth;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return this.pass;
    }

    @Override
    public String getUsername() {
        return this.user;
    }
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }
}
