package com.security.demo.config;

import com.security.demo.JwtFilter;
import com.security.demo.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@EnableWebSecurity
@Configuration
public class Websecurity {
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    JwtFilter jwt;
    @Autowired
    OAuth2AuthenticationSuccessHandler successHandler;
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // 👈 your frontend URL
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        security.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                .cors(cors ->cors.configurationSource( corsConfigurationSource()))
                .authorizeHttpRequests(request -> request.requestMatchers("/login/**","/oauth/**","/redirect/**","/api/stream/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll().anyRequest().authenticated())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(auth->{
                    auth
                            .redirectionEndpoint(redir ->
                                    redir.baseUri("/login/oauth/*"))
//                                    .loginProcessingUrl("/login/oauth2/code/azure") // callback URL
                                    .successHandler(successHandler)                  // after Azure returns
                                    .failureHandler((request, response, exception) -> {
                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                                    });

                })
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
        return security.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
       DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
       daoAuthenticationProvider.setUserDetailsService(userDetailsService);
       daoAuthenticationProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
       return daoAuthenticationProvider;
    }
}
