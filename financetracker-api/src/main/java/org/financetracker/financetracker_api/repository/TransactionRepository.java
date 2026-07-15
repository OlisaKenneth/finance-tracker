package org.financetracker.financetracker_api.repository;

import org.financetracker.financetracker_api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // finds a transaction by its category
    Optional<Transaction> findByCategory(String category);

    // THE PER-USER FILTER — returns only this user's transactions
    // SELECT * FROM transactions WHERE user_id = ?
    List<Transaction> findAllByUserId(Long userId);

    /*
     * NEW DUPLICATE CHECKER — uses Plaid's own transaction ID
     *
     * Every real Plaid transaction has a unique ID that never changes.
     * Spring generates:
     * SELECT COUNT(*) FROM transactions WHERE plaid_transaction_id = ?
     *
     * Returns true  = already saved, skip it
     * Returns false = brand new, safe to insert
     *
     * This works correctly for real bank accounts because
     * Plaid assigns each transaction a permanent unique ID.
     */
    boolean existsByPlaidTransactionId(String plaidTransactionId);
}