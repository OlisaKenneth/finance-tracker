package org.financetracker.financetracker_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/*
 * This class is our REQUEST HANDLER (the front door of our API)
 * It receives HTTP requests from Postman or a browser
 * and decides what to do with them
 *
 * Think of it like a receptionist:
 * - Someone comes in asking for something (HTTP request)
 * - Receptionist listens and passes it to the right department (Service)
 * - Gets the result and hands it back to the person (HTTP response)
 */
@RestController // tells Spring Boot "this class handles HTTP requests and returns JSON"
@RequestMapping("/api/budgets") // all URLs in this class start with /api/budgets
public class BudgetController {

    // we need the service to handle the business logic
    // Spring Boot hands it to us automatically (dependency injection)
    private BudgetService budgetService;

    /*
     * Constructor — Spring Boot sees we need a BudgetService
     * and automatically passes one in (dependency injection)
     */
    public BudgetController(BudgetService budgetService){
        this.budgetService = budgetService;
    }

    /*
     * This method handles GET requests to /api/budgets
     * When someone visits localhost:8080/api/budgets in a browser or Postman
     * this method runs and returns ALL budgets as a JSON list
     *
     * Example response:
     * [{"id":1,"category":"Groceries","monthlyLimit":500.0,"spent":0.0}]
     */
    @GetMapping // handles GET requests — used for READING data
    public List<Budget> getAllBudgets(){
        return budgetService.getAllBudget(); // ask the service to get all budgets
    }

    /*
     * This method handles POST requests to /api/budgets
     * When someone sends a new budget via Postman or a form
     * this method receives it, saves it and returns the saved budget
     *
     * @RequestBody means: take the JSON from the request body
     * and convert it into a Budget object automatically (Jackson does this)
     *
     * Example request body sent by user:
     * {"category": "Groceries", "monthlyLimit": 500.0}
     *
     * Example response sent back:
     * {"id": 1, "category": "Groceries", "monthlyLimit": 500.0, "spent": 0.0}
     */
    @PostMapping // handles POST requests — used for CREATING data
    public Budget createBudget(@RequestBody Budget budget){
        // pass the category and limit to the service to handle the creation
        return budgetService.createBudget(budget.getCategory(), budget.getMonthlyLimit());
    }



    /*
     * This method handles PUT requests to /api/budgets/{id}
     * The {id} in the URL is the id of the budget to update
     * Example: PUT localhost:8080/api/budgets/1
     *
     * @PathVariable means: take the {id} from the URL
     * and use it as the id parameter in this method
     *
     * @RequestBody means: take the JSON from the request body
     * and convert it to a Budget object
     */
    @PutMapping("/{id}") // handles PUT requests to /api/budgets/{id}
    public ResponseEntity<Budget> updateBudget(
            @PathVariable Long id,        // grabs the id from the URL
            @RequestBody Budget budget) { // grabs the new data from the request body

        // ask the service to update the budget
        Optional<Budget> updated = budgetService.updateBudget(
                id,
                budget.getCategory(),
                budget.getMonthlyLimit()
        );

        // if the budget was found and updated, return it with status 200 OK
        // if not found, return status 404 NOT FOUND
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * This method handles DELETE requests to /api/budgets/{id}
     * Example: DELETE localhost:8080/api/budgets/1
     *
     * Returns 200 OK if deleted successfully
     * Returns 404 NOT FOUND if no budget with that id exists
     */
    @DeleteMapping("/{id}") // handles DELETE requests to /api/budgets/{id}
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        // ask the service to delete the budget
        boolean deleted = budgetService.deleteBudget(id);

        // if deleted successfully return 200 OK
        // if not found return 404 NOT FOUND
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}