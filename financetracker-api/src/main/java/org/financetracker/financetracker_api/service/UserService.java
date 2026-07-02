package org.financetracker.financetracker_api.service;
import org.financetracker.financetracker_api.model.User;
import org.financetracker.financetracker_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.financetracker.financetracker_api.service.JwtService;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(String name, String email, String password, String role){
        // check if email already exists before creating anything
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        String hash = passwordEncoder.encode(password);
        newUser.setPassword(hash);
        newUser.setRole(role);

        return userRepository.save(newUser);
    }

//    /*
//     * This method handles LOGIN
//     * Steps:
//     * 1. Find the user by email — if not found, reject
//     * 2. Check if the typed password matches the saved hash
//     * 3. If it matches, generate and return a JWT token
//     * 4. If it doesn't match, reject
//     */
//    public String login(String email, String password) {
//        // step 1: find the user by email
//        Optional<User> existing = userRepository.findByEmail(email);
//
//        if (existing.isEmpty()) {
//            throw new IllegalArgumentException("Invalid email or password");
//        }
//
//        User user = existing.get();
//
//        // step 2: check if the typed password matches the saved hash
//        boolean matches = passwordEncoder.matches(password, user.getPassword());
//
//        if (!matches) {
//            throw new IllegalArgumentException("Invalid email or password");
//        }
//
//        // step 3: generate and return a token
//        return jwtService.generateToken(user.getEmail());
//    }

    public String login(String email, String password) {
        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = existing.get();

        boolean matches = passwordEncoder.matches(password, user.getPassword());

        if (!matches) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // temporary: print the real error if token generation fails
        try {
            return jwtService.generateToken(user.getEmail());
        } catch (Exception e) {
            System.out.println("TOKEN GENERATION FAILED: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }



}
