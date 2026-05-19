package org.financetracker.financetracker_api;

import org.springframework.stereotype.Service;
import java.util.List;

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
}