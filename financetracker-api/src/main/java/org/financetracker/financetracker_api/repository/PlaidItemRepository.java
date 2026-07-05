package org.financetracker.financetracker_api.repository;

import org.financetracker.financetracker_api.model.PlaidItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaidItemRepository extends JpaRepository<PlaidItem, Long> {

    // Get all bank connections belonging to one specific user —
    // same "only fetch MY rows" pattern as BudgetRepository.
    List<PlaidItem> findAllByUserId(Long userId);

    // Handy for later: quickly check if a user has ANY bank
    // connected yet (returns the first one, if it exists).
    Optional<PlaidItem> findFirstByUserId(Long userId);
}