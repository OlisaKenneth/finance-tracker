package org.financetracker.financetracker_api.config;

import org.springframework.context.annotation.Bean; //<- lets us create a reusable object Spring manages
import org.springframework.context.annotation.Configuration; //<- marks this as a settings/config class
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity; //<- lets us configure which URLs need login
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; //<- turns on custom security rules
import org.springframework.security.config.http.SessionCreationPolicy; //<- controls how Spring Security tracks logged in users
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; //<- the actual list of security rules for incoming requests
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity // tells Spring Boot "use MY custom security rules, not the default locked-down behavior"
public class SecurityConfig {

    // JwtAuthFilter needs to run BEFORE Spring's normal login check,
    // so we inject it here and place it into the filter chain below
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /*
     * This method defines WHICH URLs require login and WHICH don't
     *
     * Now:
     * - /api/auth/** (register, login) → open to everyone, no login needed
     *   (you can't require a token to log in — that's the whole point
     *   of login, to GET a token)
     * - everything else → requires a valid JWT token
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disables a browser-form protection we don't need for a REST API
                .sessionManagement(session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        // STATELESS means: don't remember who's logged in using cookies/sessions
                        // we use tokens (JWT) instead
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight checks never carry a token — always let them through
                        .requestMatchers("/api/auth/**").permitAll() // anyone can register/login without being logged in
                        .anyRequest().authenticated() // everything else now REQUIRES a valid token
                )
                // JwtAuthFilter runs before Spring's built-in login-check filter,
                // so by the time Spring checks "is this user authenticated?",
                // our filter has already read the token and set the user if valid
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}