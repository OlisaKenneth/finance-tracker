package org.financetracker.financetracker_api.service;
import org.financetracker.financetracker_api.model.Budget;
import org.financetracker.financetracker_api.repository.BudgetRepository;
import org.financetracker.financetracker_api.model.Transaction;
import org.financetracker.financetracker_api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class TransactionService {
    private TransactionRepository transactionRepository;
    private BudgetRepository budgetRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    public List<Transaction> getAllTransaction(){
        return transactionRepository.findAll();
    }

    public Transaction createTransaction(double amount, String category, String description) {
        LocalDate today = LocalDate.now();
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setCategory(category);
        transaction.setDescription(description);
        transaction.setDate(today.toString());
        transactionRepository.save(transaction);

        // find the matching budget and update spent
        Optional<Budget> budget = budgetRepository.findByCategory(category);
        if (budget.isPresent()) {
            Budget b = budget.get();
            b.setSpent(b.getSpent() + amount); // ← add the amount to spent
            budgetRepository.save(b);           // ← save back to DB
        }

        return transaction;
    }

    public boolean deleteTransaction(Long id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}