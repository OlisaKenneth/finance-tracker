package org.financetracker.financetracker_api.repository;

import org.financetracker.financetracker_api.model.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/*
 * This interface is our DATABASE HANDLER for savings goals
 * Think of it like a remote control for the savings_goal table
 * We extend JpaRepository which means Spring Boot gives us
 * free database operations without writing any SQL:
 *
 * findAll()        → SELECT * FROM savings_goal (get all goals)
 * findById(1L)     → SELECT * FROM savings_goal WHERE id = 1
 * save(goal)       → INSERT or UPDATE a savings goal
 * deleteById(1L)   → DELETE FROM savings_goal WHERE id = 1
 *
 * JpaRepository<SavingsGoal, Long> means:
 * SavingsGoal → we are managing SavingsGoal objects
 * Long        → the primary key (id) is of type Long
 */
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long>{

    /*
     * This is a custom method we added ourselves
     * Spring Boot reads the method name "findByGoalName"
     * and automatically generates this SQL:
     * SELECT * FROM savings_goal WHERE goal_name = ?
     *
     * Optional<SavingsGoal> means the result might be empty
     * if no goal with that name exists
     */
    Optional<SavingsGoal> findByGoalName(String goalName);

    /*
     * THE PER-USER FILTER — same pattern as the other repositories.
     *
     * Spring Boot reads "findAllByUserId" and generates:
     * SELECT * FROM savings_goal WHERE user_id = ?
     *
     * This ensures each logged-in user only ever sees their
     * own savings goals, never anyone else's.
     */
    List<SavingsGoal> findAllByUserId(Long userId);
}