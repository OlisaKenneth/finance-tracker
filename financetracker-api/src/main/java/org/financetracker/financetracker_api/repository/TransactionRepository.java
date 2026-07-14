package org.financetracker.financetracker_api.repository;

import org.financetracker.financetracker_api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{

    // finds a transaction by its category
    Optional<Transaction> findByCategory(String category);

    // THE PER-USER FILTER — returns only this user's transactions
    // SELECT * FROM transactions WHERE user_id = ?
    List<Transaction> findAllByUserId(Long userId);

    // THE DUPLICATE CHECKER — NEW
    // Spring reads this name and generates:
    // SELECT COUNT(*) FROM transactions
    // WHERE user_id=? AND amount=? AND date=? AND description=?
    // Returns true if this transaction already exists in the DB
    // Returns false if it is brand new and safe to insert
    boolean existsByUserIdAndAmountAndDateAndDescription(
            Long userId,
            double amount,
            String date,
            String description
    );
}