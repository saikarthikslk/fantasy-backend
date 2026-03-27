package com.security.demo;

import com.security.demo.config.Userdetailsservice;
import com.security.demo.config.Websecurity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    JwtService jwtService;
    @Autowired
    Userdetailsservice userdetailsservice;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
         String token =   request.getHeader("key");
         if(token != null) {
             String username =  jwtService.extractUsername(token);
             if(username != null) {
                 UserDetails userDetails = userdetailsservice.loadUserByUsername(username);

                 if (jwtService.isTokenValid(token, userDetails)) {
                     UsernamePasswordAuthenticationToken utoken = new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
                     utoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                     SecurityContextHolder.getContext().setAuthentication(utoken);

                 }






             }

         }
         filterChain.doFilter(request,response);


    }
}
