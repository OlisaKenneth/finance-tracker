package org.financetracker.financetracker_api.repository;

import org.financetracker.financetracker_api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{

    Optional<Transaction> findByCategory(String category);
}
