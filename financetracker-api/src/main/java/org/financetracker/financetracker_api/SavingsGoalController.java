package org.financetracker.financetracker_api;

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
     * When someone visits localhost:8080/api/savings_goal
     * this method runs and returns ALL savings goals as a JSON list
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
     * When someone sends a new savings goal via Postman or a form
     * this method receives it, saves it and returns the saved goal
     *
     * Example request body:
     * {"goalName": "Car", "targetAmount": 8000.0, "months": 24}
     *
     * Example response:
     * {"id": 1, "goalName": "Car", "targetAmount": 8000.0, "savedSoFar": 0.0, "months": 24}
     */
    @PostMapping // handles POST requests — used for CREATING data
    public SavingsGoal createSavingsGoal(@RequestBody SavingsGoal savingsGoal){
        // pass the goal details to the service to handle the creation
        return savingsGoalService.createGoal(
                savingsGoal.getGoalName(),
                savingsGoal.getTargetAmount(),
                savingsGoal.getMonths()
        );
    }

    /*
     * This method handles PUT requests to /api/savings_goal/{goalName}/add
     * It adds money to an existing savings goal
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