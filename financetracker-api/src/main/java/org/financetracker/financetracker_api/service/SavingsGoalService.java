package org.financetracker.financetracker_api.service;

import org.financetracker.financetracker_api.model.SavingsGoal;
import org.financetracker.financetracker_api.repository.SavingsGoalRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/*
 * This class is our SAVINGS GOAL LOGIC HANDLER
 * It sits between the Controller and the Repository
 * Controller receives requests → Service decides what to do → Repository talks to DB
 *
 * Think of it like a manager:
 * - The waiter (Controller) takes the request
 * - The manager (Service) checks if it makes sense
 * - The kitchen (Repository) actually does the database work
 */
@Service // tells Spring Boot "manage this class for me and inject it where needed"
public class SavingsGoalService {

    // we need the repository to talk to the database
    // we don't create it ourselves — Spring Boot hands it to us (dependency injection)
    private SavingsGoalRepository savingsGoalRepository;

    /*
     * Constructor — Spring Boot sees that SavingsGoalService needs a SavingsGoalRepository
     * so it automatically creates one and passes it in here
     * This is called DEPENDENCY INJECTION
     */
    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    /*
     * This method gets ALL savings goals from the database
     * It returns a List because there can be many goals
     * findAll() is a free method from JpaRepository
     * it runs: SELECT * FROM savings_goal
     */
    public List<SavingsGoal> getAllGoals(){
        return savingsGoalRepository.findAll();
    }

    /*
     * This method creates a NEW savings goal and saves it to the database
     * Steps:
     * 1. Create a new empty SavingsGoal object
     * 2. Fill in the goalName, targetAmount and months from what the user sent
     * 3. Set savedSoFar to 0 because a new goal has nothing saved yet
     * 4. Save it to the database and return the saved goal (now with an id)
     */
    public SavingsGoal createGoal(String goalName, double targetAmount, int months){
        SavingsGoal savingsGoal = new SavingsGoal(); // step 1: create empty goal
        savingsGoal.setGoalName(goalName);       // step 2a: set what we are saving for
        savingsGoal.setTargetAmount(targetAmount); // step 2b: set the target amount
        savingsGoal.setMonths(months);             // step 2c: set how many months
        savingsGoal.setSavedSoFar(0);              // step 3: nothing saved yet
        return savingsGoalRepository.save(savingsGoal); // step 4: save to DB and return
    }

    /*
     * This method adds money to an existing savings goal
     * Steps:
     * 1. Find the goal by its name in the database
     * 2. If it exists, add the value to what is already saved
     * 3. Save the updated goal back to the database
     * 4. Return the updated goal
     *
     * Returns Optional.empty() if the goal name does not exist
     */
    public Optional<SavingsGoal> addSavings(String goalName, double value){
        // step 1: find the goal by name — might be empty if name does not exist
        Optional<SavingsGoal> existing = savingsGoalRepository.findByGoalName(goalName);

        if(existing.isPresent()){ // step 2: only proceed if the goal exists
            SavingsGoal savingsGoal = existing.get(); // get the actual object out of Optional
            double currentValue = savingsGoal.getSavedSoFar() + value; // add new money to existing savings
            savingsGoal.setSavedSoFar(currentValue); // update the savedSoFar field
            savingsGoalRepository.save(savingsGoal); // step 3: save back to database
            return Optional.of(savingsGoal); // step 4: return the updated goal
        }
        return Optional.empty(); // goal not found — return empty
    }
}