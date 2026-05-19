package org.financetracker.financetracker_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * This is the STARTING POINT of the entire application
 * When you click Run in IntelliJ, Java starts here
 * Think of it like the ignition key of a car
 */
@SpringBootApplication // this one annotation does three things:
// 1. marks this as a Spring Boot application
// 2. tells Spring to scan all classes for @Service, @RestController etc.
// 3. enables auto-configuration (sets up database, server etc. automatically)
public class FinancetrackerApiApplication {

    /*
     * main() is where Java always starts
     * SpringApplication.run() starts the whole Spring Boot engine:
     * - starts Tomcat web server on port 8080
     * - connects to the H2 database
     * - creates all the @Service and @Repository objects
     * - gets everything ready to receive requests
     */
    public static void main(String[] args) {
        SpringApplication.run(FinancetrackerApiApplication.class, args);
    }
}