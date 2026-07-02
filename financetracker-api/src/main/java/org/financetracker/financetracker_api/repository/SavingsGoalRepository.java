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
     * THE PER-USER FILTER
     *
     * Spring Boot reads "findAllByUserId" and generates:
     * SELECT * FROM savings_goal WHERE user_id = ?
     *
     * This ensures each logged-in user only ever sees their
     * own savings goals, never anyone else's.
     */
    List<SavingsGoal> findAllByUserId(Long userId);

    /*
     * THE SCOPED GOAL NAME LOOKUP
     *
     * Replaces the old findByGoalName(String goalName), which
     * searched the ENTIRE table with no owner check — meaning
     * two different users could each name a goal "Car" and
     * accidentally collide.
     *
     * Spring Boot reads "findByGoalNameAndUserId" and generates:
     * SELECT * FROM savings_goal WHERE goal_name = ? AND user_id = ?
     *
     * Now this lookup is naturally scoped to one specific user
     * at the database level.
     */
    Optional<SavingsGoal> findByGoalNameAndUserId(String goalName, Long userId);
}