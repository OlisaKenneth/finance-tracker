package org.financetracker.financetracker_api.service;

import org.financetracker.financetracker_api.model.SavingsGoal;
import org.financetracker.financetracker_api.model.User;
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
 *
 * NEW IN THIS VERSION: every method now also asks
 * "which user does this belong to?" so one person can
 * never see or touch another person's savings goals.
 */
@Service // tells Spring Boot "manage this class for me and inject it where needed"
public class SavingsGoalService {

    // we need the repository to talk to the database
    // we don't create it ourselves — Spring Boot hands it to us (dependency injection)
    private SavingsGoalRepository savingsGoalRepository;

    // NEW: lets us ask "who is currently logged in?"
    private CurrentUserService currentUserService;

    /*
     * Constructor — Spring Boot sees that SavingsGoalService needs
     * a SavingsGoalRepository and a CurrentUserService, so it
     * automatically creates both and passes them in here.
     * This is called DEPENDENCY INJECTION
     */
    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository, CurrentUserService currentUserService) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.currentUserService = currentUserService;
    }

    /*
     * Returns only the savings goals belonging to whoever is
     * currently logged in — not everyone's.
     */
    public List<SavingsGoal> getAllGoals(){
        Long userId = currentUserService.getCurrentUserId();
        return savingsGoalRepository.findAllByUserId(userId);
    }

    /*
     * Creates a new savings goal, automatically owned by the
     * logged-in user.
     */
    public SavingsGoal createGoal(String goalName, double targetAmount, int months){
        SavingsGoal savingsGoal = new SavingsGoal(); // step 1: create empty goal
        savingsGoal.setGoalName(goalName);       // step 2a: set what we are saving for
        savingsGoal.setTargetAmount(targetAmount); // step 2b: set the target amount
        savingsGoal.setMonths(months);             // step 2c: set how many months
        savingsGoal.setSavedSoFar(0);              // step 3: nothing saved yet

        User owner = currentUserService.getCurrentUser(); // step 4: find who's logged in
        savingsGoal.setUser(owner);                        // step 4: attach them as the owner

        return savingsGoalRepository.save(savingsGoal); // step 5: save to DB and return
    }

    /*
     * Adds money to an existing savings goal — but ONLY if a
     * goal with that name belongs to whoever is currently
     * logged in. Two different users could each name a goal
     * "Car", so we search by name AND ownership together,
     * scoped at the database level via findByGoalNameAndUserId.
     */
    public Optional<SavingsGoal> addSavings(String goalName, double value){
        Long userId = currentUserService.getCurrentUserId();

        Optional<SavingsGoal> existing = savingsGoalRepository.findByGoalNameAndUserId(goalName, userId);

        if (existing.isPresent()) {
            SavingsGoal savingsGoal = existing.get();
            double currentValue = savingsGoal.getSavedSoFar() + value;
            savingsGoal.setSavedSoFar(currentValue);
            savingsGoalRepository.save(savingsGoal);
            return Optional.of(savingsGoal);
        }
        return Optional.empty();
    }
}