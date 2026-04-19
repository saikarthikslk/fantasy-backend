package com.security.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.security.demo.DBmodel.Acitivity;
import com.security.demo.config.Userdetailsservice;
import com.security.demo.config.Websecurity;
import com.security.demo.model.Tracking;
import com.security.demo.repo.ActivityRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Transient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;

@Component
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    JwtService jwtService;
    @Autowired
    Userdetailsservice userdetailsservice;
    @Autowired
    ActivityRepo activityRepo;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
         String token =   request.getHeader("key");
         if(token != null) {
             String username =  jwtService.extractUsername(token);
             if(username != null) {
                 UserDetails userDetails = userdetailsservice.loadUserByUsername(username);

                 if (jwtService.isTokenValid(token, userDetails)) {
                     Acitivity acitivity = new Acitivity();
                     acitivity.setTimestamp(Timestamp.from(Instant.now()));
                     acitivity.setTrack(buildbody(request));
                     acitivity.setEmail(userDetails.getUsername());
                     activityRepo.save(acitivity);
                     UsernamePasswordAuthenticationToken utoken = new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
                     utoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                     SecurityContextHolder.getContext().setAuthentication(utoken);

                 }

             }

         }
         filterChain.doFilter(request,response);


    }
    private static ObjectMapper objectMapper = new ObjectMapper();
    public String buildbody(HttpServletRequest request) throws IOException {
        Tracking tracking = new Tracking();
        tracking.setPath(request.getRequestURI());
        tracking.setAddr(request.getRemoteAddr());
        tracking.setHost(request.getRemoteHost());
        if(!tracking.getPath().contains("/api/user")){
            InputStream is = request.getInputStream();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            tracking.setBody(body);
        }
        return objectMapper.writeValueAsString(tracking);

    }
}
