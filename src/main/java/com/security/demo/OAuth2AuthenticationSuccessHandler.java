package com.security.demo;

import com.security.demo.DBmodel.User;
import com.security.demo.config.Userdetailsservice;
import com.security.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {
    @Autowired
    private  JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private Userdetailsservice userdetailsservice;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // ① Get user info from Azure
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email"); // Azure claim
        String name  = oAuth2User.getAttribute("name");
        User user = userService.createifnotExists(email,name);
        UserDetails userDetails = convert(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ③ Generate YOUR JWT

        String token = jwtService.generateToken(userDetails);

        // ④ Return token — two options below:

        // Option A — return JWT in response body (for REST/SPA)
        response.setContentType("application/json");
        response.getWriter().write("{\"token\": \"" + token + "\"}");
        response.sendRedirect("https://fantasy-frontend-loqr.vercel.app/#/matches?token="+token);
//        response.sendRedirect("https://fantasy-frontend-loqr.vercel.app/#/matches?token="+token);
        // Option B — redirect to frontend with token in URL (for web apps)
    }

    public UserDetails convert(User user) {
        return new com.security.demo.config.User(user.getEmail(), "", "");
    }
}