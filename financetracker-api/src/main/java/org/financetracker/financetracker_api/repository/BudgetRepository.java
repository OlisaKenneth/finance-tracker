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
     * THE PER-USER FILTER
     *
     * Spring Boot reads "findAllByUserId" and generates:
     * SELECT * FROM budgets WHERE user_id = ?
     *
     * This is how we make sure one person can never see
     * another person's budgets — we only ever ask the
     * database for rows that belong to the logged-in user.
     */
    List<Budget> findAllByUserId(Long userId);

    /*
     * THE SCOPED CATEGORY LOOKUP
     *
     * Replaces the old findByCategory(String category), which
     * searched the ENTIRE table with no owner check — meaning
     * two different users with a "Groceries" budget could
     * accidentally collide.
     *
     * Spring Boot reads "findByCategoryAndUserId" and generates:
     * SELECT * FROM budgets WHERE category = ? AND user_id = ?
     *
     * Now this lookup is naturally scoped to one specific user
     * at the database level — no manual filtering needed
     * afterward in the service layer.
     */
    Optional<Budget> findByCategoryAndUserId(String category, Long userId);
}