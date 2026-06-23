package org.financetracker.financetracker_api.repository;

import org.financetracker.financetracker_api.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;//A powerful tool that gives you instant database actions
                                                    // (like save, delete, and find) without writing any database code.
import java.util.*;

/*
 * This interface is our DATABASE HANDLER
 * Think of it like a remote control for the database
 * We extend JpaRepository which means Spring Boot gives us
 * free database operations without writing any SQL:
 *
 * findAll()          → SELECT * FROM budgets (get all budgets)
 * findById(1L)       → SELECT * FROM budgets WHERE id = 1
 * save(budget)       → INSERT or UPDATE a budget
 * deleteById(1L)     → DELETE FROM budgets WHERE id = 1
 *
 * JpaRepository<Budget, Long> means:
 * Budget → we are managing Budget objects
 * Long   → the primary key (id) is of type Long
 */
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /*
     * This is a custom method we added ourselves
     * Spring Boot reads the method name "findByCategory"
     * and automatically generates this SQL:
     * SELECT * FROM budgets WHERE category = ?
     *
     * Optional<Budget> means the result might be empty
     * (if no budget with that category exists)
     */
    Optional<Budget> findByCategory(String category);
}

/* budgetRepository.findAll()        ← SELECT * FROM budgets
budgetRepository.findById(1L)     ← SELECT * WHERE id = 1
budgetRepository.save(budget)     ← INSERT or UPDATE
budgetRepository.deleteById(1L)   ← DELETE WHERE id = 1

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByCategory(String category);
}

SELECT * FROM budgets WHERE category = ?
*/