package org.financetracker.financetracker_api.service;
import org.financetracker.financetracker_api.model.Budget;
import org.financetracker.financetracker_api.model.User;
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

    // NEW: lets us ask "who is currently logged in?"
    private CurrentUserService currentUserService;

    public TransactionService(TransactionRepository transactionRepository,
                              BudgetRepository budgetRepository,
                              CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.currentUserService = currentUserService;
    }

    /*
     * Returns only the transactions belonging to whoever is
     * currently logged in — not everyone's.
     */
    public List<Transaction> getAllTransaction(){
        Long userId = currentUserService.getCurrentUserId();
        return transactionRepository.findAllByUserId(userId);
    }

    /*
     * Creates a new transaction, automatically owned by the
     * logged-in user, and updates the MATCHING budget's spent
     * amount — but only if that matching budget ALSO belongs
     * to this same user. Without that check, spending in one
     * category could accidentally update a stranger's budget
     * if they happened to use the same category name.
     */
    public Transaction createTransaction(double amount, String category, String description) {
        User owner = currentUserService.getCurrentUser();

        LocalDate today = LocalDate.now();
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setCategory(category);
        transaction.setDescription(description);
        transaction.setDate(today.toString());
        transaction.setUser(owner); // attach the logged-in user as owner
        transactionRepository.save(transaction);

        // find the matching budget for THIS user and update spent.
        // findByCategoryAndUserId is scoped at the database level,
        // so it can only ever return a budget owned by this user —
        // two different people can safely each have a "Groceries"
        // budget without colliding.
        Optional<Budget> budget = budgetRepository.findByCategoryAndUserId(category, owner.getId());
        if (budget.isPresent()) {
            Budget b = budget.get();
            b.setSpent(b.getSpent() + amount); // ← add the amount to spent
            budgetRepository.save(b);           // ← save back to DB
        }

        return transaction;
    }

    /*
     * Deletes a transaction — but ONLY if it belongs to whoever
     * is currently logged in.
     */
    public boolean deleteTransaction(Long id) {
        Long userId = currentUserService.getCurrentUserId();

        Optional<Transaction> existing = transactionRepository.findById(id);

        if (existing.isPresent() && existing.get().getUser().getId().equals(userId)) {
            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}