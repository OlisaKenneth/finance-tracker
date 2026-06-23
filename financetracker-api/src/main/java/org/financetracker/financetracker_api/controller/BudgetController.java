package org.financetracker.financetracker_api.controller;

import jakarta.validation.Valid;//<- Tells Spring to automatically trigger a validation check on an incoming object.
import org.financetracker.financetracker_api.model.Budget;
import org.financetracker.financetracker_api.service.BudgetService;
import org.springframework.http.ResponseEntity;//<-A container that holds the data you want to send back to a user,
                                                        // along with web status codes (like 200 OK or 404 Not Found).
import org.springframework.web.bind.annotation.*;//imports web controls. For example, @RestController makes your class
                                   // handle web requests, and @GetMapping maps a specific web address to a Java method.
import java.util.List;//<-An ordered list of items (like a list of users).
import java.util.Optional;//<-A safety container used when a piece of data might be missing, helping prevent app crashes.

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
     * this method receives it, validates it, saves it and returns the saved budget
     *
     * @Valid tells Spring Boot to run the validation rules
     * defined in Budget.java (@NotBlank, @Positive) BEFORE
     * this method runs — if validation fails it returns 400 Bad Request
     * automatically with the error message we defined
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
    public Budget createBudget(@Valid @RequestBody Budget budget){
        // @Valid runs the validation rules before this line executes
        // if category is blank or monthlyLimit is negative → rejected before reaching here
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
     * @Valid runs validation rules on the incoming budget data
     */
    @PutMapping("/{id}") // handles PUT requests to /api/budgets/{id}
    public ResponseEntity<Budget> updateBudget(
            @PathVariable Long id,              // grabs the id from the URL
            @Valid @RequestBody Budget budget){ // grabs and validates the new data

        // ask the service to update the budget
        Optional<Budget> updated = budgetService.updateBudget(
                id,
                budget.getCategory(),
                budget.getMonthlyLimit()
        );

        // if the budget was found and updated return it with 200 OK
        // if not found return 404 NOT FOUND
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
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id){
        // ask the service to delete the budget
        boolean deleted = budgetService.deleteBudget(id);

        // if deleted successfully return 200 OK
        // if not found return 404 NOT FOUND
        if(deleted){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}