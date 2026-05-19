package org.financetracker.financetracker_api;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/*
 * This class is our BUDGET LOGIC HANDLER
 * It sits between the Controller and the Repository
 * Controller receives requests → Service decides what to do → Repository talks to DB
 *
 * Think of it like a manager at a restaurant:
 * - The waiter (Controller) takes the order
 * - The manager (Service) checks if the order makes sense
 * - The kitchen (Repository) actually prepares it
 */
@Service // tells Spring Boot "manage this class for me and inject it where needed"
public class BudgetService {

    // we need the repository to talk to the database
    // we don't create it ourselves — Spring Boot hands it to us (dependency injection)
    private BudgetRepository budgetRepository;

    /*
     * This is the constructor
     * Spring Boot sees that BudgetService needs a BudgetRepository
     * so it automatically creates one and passes it in here
     * This is called DEPENDENCY INJECTION
     */
    public BudgetService(BudgetRepository budgetRepository){
        this.budgetRepository = budgetRepository;
    }

    /*
     * This method gets ALL budgets from the database
     * It returns a List because there can be many budgets
     * findAll() is a free method from JpaRepository
     * it runs: SELECT * FROM budgets
     */
    public List<Budget> getAllBudget(){
        return budgetRepository.findAll();
    }

    /*
     * This method creates a NEW budget and saves it to the database
     * Steps:
     * 1. Create a new empty Budget object
     * 2. Fill in the category and monthlyLimit from what the user sent
     * 3. Set spent to 0 because a new budget has nothing spent yet
     * 4. Save it to the database and return the saved budget (now with an id)
     */
    public Budget createBudget(String category, double monthlyLimit) {
        Budget budget = new Budget(); // step 1: create empty budget
        budget.setCategory(category);       // step 2a: set the category
        budget.setMonthlyLimit(monthlyLimit); // step 2b: set the limit
        budget.setSpent(0); // step 3: nothing spent yet
        return budgetRepository.save(budget); // step 4: save to DB and return
    }

    /*
     * This method updates an existing budget
     * Steps:
     * 1. Find the budget with the given id
     * 2. If it exists, update its fields
     * 3. Save the updated budget back to the database
     * 4. Return the updated budget
     *
     * The "?" after Budget means this might return
     * nothing if the budget doesn't exist
     */
    public Optional<Budget> updateBudget(Long id, String category, double monthlyLimit) {
        // step 1: find the budget by id
        // findById returns Optional — it might be empty if id doesn't exist
        Optional<Budget> existing = budgetRepository.findById(id);

        // step 2: if a budget with that id exists, update it
        if (existing.isPresent()) {
            Budget budget = existing.get(); // get the actual budget object
            budget.setCategory(category);        // update the category
            budget.setMonthlyLimit(monthlyLimit); // update the limit
            budgetRepository.save(budget);        // save changes to database
            return Optional.of(budget);           // return the updated budget
        }

        // step 3: if no budget found with that id, return empty
        return Optional.empty();
    }

    /*
     * This method deletes a budget from the database
     * It finds the budget by id and removes it permanently
     * Returns true if deleted successfully, false if not found
     */
    public boolean deleteBudget(Long id) {
        // check if the budget exists first
        if (budgetRepository.existsById(id)) {
            budgetRepository.deleteById(id); // delete it from database
            return true; // deleted successfully
        }
        return false; // budget with that id was not found
    }

}