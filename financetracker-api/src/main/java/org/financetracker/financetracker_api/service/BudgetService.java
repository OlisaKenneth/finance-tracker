package org.financetracker.financetracker_api.service;

import org.financetracker.financetracker_api.model.Budget;
import org.financetracker.financetracker_api.model.User;
import org.financetracker.financetracker_api.repository.BudgetRepository;
import org.springframework.stereotype.Service;//Labels a class as a "Service," meaning it contains the main business
// logic and calculations of your app.
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
 *
 * NEW IN THIS VERSION: every method now also asks
 * "which user does this belong to?" so one person can
 * never see or touch another person's budgets.
 */
@Service // tells Spring Boot "manage this class for me and inject it where needed"
public class BudgetService {

    // we need the repository to talk to the database
    // we don't create it ourselves — Spring Boot hands it to us (dependency injection)
    private BudgetRepository budgetRepository;

    // NEW: lets us ask "who is currently logged in?"
    private CurrentUserService currentUserService;

    /*
     * This is the constructor
     * Spring Boot sees that BudgetService needs a BudgetRepository
     * and a CurrentUserService, so it automatically creates both
     * and passes them in here.
     * This is called DEPENDENCY INJECTION
     */
    public BudgetService(BudgetRepository budgetRepository, CurrentUserService currentUserService){
        this.budgetRepository = budgetRepository;
        this.currentUserService = currentUserService;
    }

    /*
     * This method gets budgets from the database — but ONLY
     * the ones belonging to whoever is currently logged in.
     *
     * BEFORE: budgetRepository.findAll()
     *   → SELECT * FROM budgets  (everyone's budgets)
     *
     * NOW:    budgetRepository.findAllByUserId(userId)
     *   → SELECT * FROM budgets WHERE user_id = ?  (only theirs)
     */
    public List<Budget> getAllBudget(){
        Long userId = currentUserService.getCurrentUserId();
        return budgetRepository.findAllByUserId(userId);
    }

    /*
     * This method creates a NEW budget and saves it to the database,
     * automatically owned by whoever is currently logged in.
     *
     * Steps:
     * 1. Create a new empty Budget object
     * 2. Fill in the category and monthlyLimit from what the user sent
     * 3. Set spent to 0 because a new budget has nothing spent yet
     * 4. NEW: attach the logged-in user as the owner
     * 5. Save it to the database and return the saved budget (now with an id)
     */
    public Budget createBudget(String category, double monthlyLimit) {
        Budget budget = new Budget(); // step 1: create empty budget
        budget.setCategory(category);       // step 2a: set the category
        budget.setMonthlyLimit(monthlyLimit); // step 2b: set the limit
        budget.setSpent(0); // step 3: nothing spent yet

        User owner = currentUserService.getCurrentUser(); // step 4: find who's logged in
        budget.setUser(owner);                             // step 4: attach them as the owner

        return budgetRepository.save(budget); // step 5: save to DB and return
    }

    /*
     * This method updates an existing budget — but ONLY if it
     * actually belongs to whoever is currently logged in. This
     * stops User A from updating User B's budget just by
     * guessing their budget's id number in the URL.
     *
     * Steps:
     * 1. Find the budget with the given id
     * 2. If it exists AND belongs to the current user, update it
     * 3. Save the updated budget back to the database
     * 4. Return the updated budget
     *
     * If the budget doesn't exist, OR exists but belongs to
     * someone else, we return empty — the controller will then
     * respond with 404 either way, so an attacker can't even
     * tell the difference between "doesn't exist" and
     * "exists but isn't yours."
     */
    public Optional<Budget> updateBudget(Long id, String category, double monthlyLimit) {
        Long userId = currentUserService.getCurrentUserId();

        // step 1: find the budget by id
        Optional<Budget> existing = budgetRepository.findById(id);

        // step 2: only update if it exists AND belongs to this user
        if (existing.isPresent() && existing.get().getUser().getId().equals(userId)) {
            Budget budget = existing.get(); // get the actual budget object
            budget.setCategory(category);        // update the category
            budget.setMonthlyLimit(monthlyLimit); // update the limit
            budgetRepository.save(budget);        // save changes to database
            return Optional.of(budget);           // return the updated budget
        }

        // step 3: not found, or found but not yours — return empty either way
        return Optional.empty();
    }

    /*
     * This method deletes a budget — but ONLY if it belongs to
     * whoever is currently logged in. Same ownership check as
     * updateBudget above.
     */
    public boolean deleteBudget(Long id) {
        Long userId = currentUserService.getCurrentUserId();

        Optional<Budget> existing = budgetRepository.findById(id);

        if (existing.isPresent() && existing.get().getUser().getId().equals(userId)) {
            budgetRepository.deleteById(id); // delete it from database
            return true; // deleted successfully
        }
        return false; // not found, or found but not yours
    }

}