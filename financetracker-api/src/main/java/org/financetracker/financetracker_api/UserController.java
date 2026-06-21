package org.financetracker.financetracker_api;

import org.springframework.web.bind.annotation.*; //<- gives us @RestController, @RequestMapping, @PostMapping, @RequestBody

/*
 * This class is our REQUEST HANDLER for authentication
 * (the front door for registering new users)
 *
 * Think of it like a receptionist:
 * - User fills a form to register
 * - Receptionist passes the info to the right department (UserService)
 * - Gets the result and hands it back to the user
 */
@RestController // tells Spring Boot "this class handles HTTP requests and returns JSON"
@RequestMapping("/api/auth") // all URLs in this class start with /api/auth
public class UserController {

    // we need the service to handle the business logic
    // Spring Boot hands it to us automatically (dependency injection)
    private UserService userService;

    /*
     * Constructor — Spring Boot sees we need a UserService
     * and automatically passes one in (dependency injection)
     */
    public UserController(UserService userService){
        this.userService = userService;
    }

    /*
     * This method handles POST requests to /api/auth/register
     * Receives a new user's details, hashes the password,
     * saves them, and returns the created user
     *
     * Example request body:
     * {"name": "Kenneth", "email": "kenneth@example.com", "password": "hello123", "role": "USER"}
     *
     * Example response:
     * {"id": 1, "name": "Kenneth", "email": "kenneth@example.com", "password": "$2a$10$...", "role": "USER"}
     */
    @PostMapping("/register") // handles POST requests to /api/auth/register
    public User register(@RequestBody User user){
        // @RequestBody takes the JSON sent by the user
        // and converts it into a User object automatically

        return userService.register(
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }
}