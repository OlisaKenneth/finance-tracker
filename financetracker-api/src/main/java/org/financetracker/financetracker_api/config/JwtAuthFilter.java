package org.financetracker.financetracker_api.config;

import jakarta.servlet.FilterChain; // <- lets the request continue to the next step after our check
import jakarta.servlet.ServletException; // <- required exception for servlet filters
import jakarta.servlet.http.HttpServletRequest; // <- represents the incoming HTTP request
import jakarta.servlet.http.HttpServletResponse; // <- represents the outgoing HTTP response
import org.financetracker.financetracker_api.service.JwtService; // <- our token reader
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // <- tells Spring "this user is authenticated"
import org.springframework.security.core.context.SecurityContextHolder; // <- Spring's holder for "who is currently logged in"
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // <- adds request details to the auth token
import org.springframework.stereotype.Component; // <- tells Spring to manage this class
import org.springframework.web.filter.OncePerRequestFilter; // <- runs this filter exactly once per request

import java.io.IOException; // <- required exception for reading request data
import java.util.Collections; // <- used to pass an empty list of permissions for now

/*
 * JwtAuthFilter — THE BOUNCER
 *
 * Every single HTTP request passes through this filter BEFORE
 * reaching any controller (BudgetController, UserController, etc.)
 *
 * What it does:
 * 1. Reads the "Authorization" header from the request
 *    (format: "Bearer eyJhbGc...")
 * 2. Pulls out just the token (removes "Bearer ")
 * 3. Reads the email stored inside the token
 * 4. Tells Spring Security "this user is authenticated"
 * 5. Lets the request continue to the controller
 *
 * If no token → just continues (SecurityConfig decides what to do)
 * If bad token → exception is thrown, request is rejected
 */
@Component // tells Spring to manage this class automatically
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter = this runs ONCE per request, guaranteed
    // no matter how many filters are in the chain

    private JwtService jwtService; // <- our token reading tool

    /*
     * Constructor — Spring injects JwtService automatically
     */
    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /*
     * This is the main method that runs on EVERY request
     *
     * Parameters:
     * request  = the incoming HTTP request (has headers, body, URL)
     * response = the outgoing HTTP response (what we send back)
     * filterChain = the chain of filters — we call this to continue
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // step 1: read the Authorization header
        // every request that has a token sends it like this:DID
        // Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
        String authHeader = request.getHeader("Authorization");

        // step 2: if there's no header or it doesn't start with "Bearer "
        // → this request has no token, just let it continue
        // SecurityConfig will decide if it needs one or not
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // continue without authenticating
            return; // stop here
        }

        // step 3: pull out just the token part
        // "Bearer eyJhbGc..." → "eyJhbGc..."
        // substring(7) removes the first 7 characters ("Bearer ")
        String token = authHeader.substring(7);

        // step 4: extract the email stored inside the token
        // if the token is expired or tampered with, this throws an exception
        String email = jwtService.extractEmail(token);

        // step 5: if we got an email AND Spring doesn't already know who this is
        // (SecurityContextHolder.getContext().getAuthentication() == null means
        //  "Spring doesn't have a logged-in user set for this request yet")
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // create an Authentication object that says "this email is verified"
            // first parameter  = who they are (email)
            // second parameter = their password (null — we already verified via token)
            // third parameter  = their permissions (empty list for now)
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

            // attach the request details (IP address, session info) to the auth token
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // step 6: tell Spring Security "this user is authenticated for this request"
            // every controller that runs after this can trust the user is logged in
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // step 7: continue to the next filter or the actual controller
        filterChain.doFilter(request, response);
    }
}