package org.financetracker.financetracker_api.controller;

import jakarta.validation.Valid;
import org.financetracker.financetracker_api.model.SavingsGoal;
import org.financetracker.financetracker_api.service.SavingsGoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/*
 * This class is our REQUEST HANDLER for savings goals (the front door of the savings API)
 * It receives HTTP requests from Postman or a browser
 * and decides what to do with them
 *
 * Think of it like a receptionist:
 * - Someone comes in asking for something (HTTP request)
 * - Receptionist listens and passes it to the right department (Service)
 * - Gets the result and hands it back to the person (HTTP response)
 */
@RestController // tells Spring Boot "this class handles HTTP requests and returns JSON"
@RequestMapping("/api/savings_goal") // all URLs in this class start with /api/savings_goal
public class SavingsGoalController {

    // we need the service to handle the business logic
    // Spring Boot hands it to us automatically (dependency injection)
    private SavingsGoalService savingsGoalService;

    /*
     * Constructor — Spring Boot sees we need a SavingsGoalService
     * and automatically passes one in (dependency injection)
     */
    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    /*
     * This method handles GET requests to /api/savings_goal
     * Returns ALL savings goals as a JSON list
     *
     * Example response:
     * [{"id":1,"goalName":"Car","targetAmount":8000.0,"savedSoFar":500.0,"months":24}]
     */
    @GetMapping // handles GET requests — used for READING data
    public List<SavingsGoal> getAllSavingsGoal(){
        return savingsGoalService.getAllGoals(); // ask the service to get all goals
    }

    /*
     * This method handles POST requests to /api/savings_goal
     * Receives a new savings goal, validates it, saves it and returns it
     *
     * @Valid runs the validation rules from SavingsGoal.java
     * (@NotBlank on goalName, @Positive on targetAmount and months)
     * BEFORE this method runs — if validation fails returns 400 Bad Request
     *
     * Example request body:
     * {"goalName": "Car", "targetAmount": 8000.0, "months": 24}
     *
     * Example response:
     * {"id":1,"goalName":"Car","targetAmount":8000.0,"savedSoFar":0.0,"months":24}
     */
    @PostMapping // handles POST requests — used for CREATING data
    public SavingsGoal createSavingsGoal(@Valid @RequestBody SavingsGoal savingsGoal){
        // @Valid validates before this line runs
        // if goalName is blank or targetAmount is negative → rejected before reaching here
        return savingsGoalService.createGoal(
                savingsGoal.getGoalName(),
                savingsGoal.getTargetAmount(),
                savingsGoal.getMonths()
        );
    }

    /*
     * This method handles PUT requests to /api/savings_goal/{goalName}/add
     * Adds money to an existing savings goal
     *
     * @PathVariable grabs goalName from the URL path:
     * /api/savings_goal/Car/add → goalName = "Car"
     *
     * @RequestParam grabs value from the ? part of the URL:
     * /api/savings_goal/Car/add?value=500 → value = 500.0
     *
     * Returns 200 OK with updated goal if found
     * Returns 404 NOT FOUND if no goal with that name exists
     */
    @PutMapping("/{goalName}/add") // handles PUT requests to /api/savings_goal/{goalName}/add
    public ResponseEntity<SavingsGoal> addSavings(
            @PathVariable String goalName,  // grabs goalName from the URL path
            @RequestParam double value){    // grabs value from ?value=500 in the URL

        // ask the service to add the money to the goal
        Optional<SavingsGoal> updated = savingsGoalService.addSavings(goalName, value);

        // if goal was found and updated return it with 200 OK
        // if not found return 404 NOT FOUND
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}