package org.financetracker.financetracker_api;

import org.springframework.context.annotation.Bean; //<- Tells Spring "create one object from this method and keep it available app-wide"
import org.springframework.context.annotation.Configuration; //<- Marks this class as a settings/config class, not a regular class
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; //<- The actual tool that scrambles (hashes) passwords securely
import org.springframework.security.crypto.password.PasswordEncoder; //<- The general "type" of tool BCryptPasswordEncoder belongs to

/*
 * This class is our SECURITY SETTINGS file
 * It doesn't contain business logic — it just creates tools
 * that other classes (like a future UserService) can use
 *
 * Think of it like a toolbox:
 * This class puts ONE tool inside the toolbox (the password scrambler)
 * Any other class can reach into the toolbox and grab it
 */
@Configuration // tells Spring Boot "this class sets up configuration, scan it on startup"
public class SecurityConfig {

    /*
     * This method creates ONE PasswordEncoder tool
     * and Spring keeps it ready to hand out to any class that asks for it
     *
     * @Bean means: "run this method once, keep the result,
     * and give it to whoever needs a PasswordEncoder"
     *
     * BCryptPasswordEncoder is the specific tool we chose
     * It takes a plain password like "hello123"
     * and turns it into a scrambled, one-way hash like:
     * "$2a$10$N9qo8uLOickgx2ZMRZoMy..."
     *
     * One-way means: you can SCRAMBLE a password
     * but you can NEVER unscramble it back to the original
     * You can only CHECK if a typed password matches the hash
     */
    @Bean // tells Spring "create this object once, share it everywhere it's needed"
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // creates the actual password-scrambling tool
    }
}