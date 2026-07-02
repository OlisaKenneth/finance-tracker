package org.financetracker.financetracker_api.repository;

import org.financetracker.financetracker_api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{

    Optional<Transaction> findByCategory(String category);

    /*
     * THE PER-USER FILTER — same pattern as BudgetRepository.
     *
     * Spring Boot reads "findAllByUserId" and generates:
     * SELECT * FROM transactions WHERE user_id = ?
     *
     * This ensures each logged-in user only ever sees their
     * own transactions, never anyone else's.
     */
    List<Transaction> findAllByUserId(Long userId);
}