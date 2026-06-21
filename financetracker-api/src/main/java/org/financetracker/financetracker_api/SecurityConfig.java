package org.financetracker.financetracker_api;

import org.springframework.context.annotation.Bean; //<- lets us create a reusable object Spring manages
import org.springframework.context.annotation.Configuration; //<- marks this as a settings/config class
import org.springframework.security.config.annotation.web.builders.HttpSecurity; //<- lets us configure which URLs need login
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; //<- turns on custom security rules
import org.springframework.security.config.http.SessionCreationPolicy; //<- controls how Spring Security tracks logged in users
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; //<- the actual list of security rules for incoming requests

@Configuration
@EnableWebSecurity // tells Spring Boot "use MY custom security rules, not the default locked-down behavior"
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * This method defines WHICH URLs require login and WHICH don't
     *
     * Right now:
     * - /api/auth/** (register, login) → open to everyone, no login needed
     * - everything else → still open for now (we'll lock it down later
     *   once login actually works and the frontend can send tokens)
     *
     * We're doing this in stages so nothing breaks while we build
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disables a browser-form protection we don't need for a REST API
                .sessionManagement(session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        // STATELESS means: don't remember who's logged in using cookies/sessions
                        // we'll use tokens (JWT) instead, added in a later step
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // anyone can register/login without being logged in
                        .anyRequest().permitAll() // TEMPORARY: everything else still open until JWT is wired in
                );

        return http.build();
    }
}