package org.financetracker;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DashboardController {

    @FXML private Button addExpenseBtn;
    @FXML private Button viewBudgetsBtn;
    @FXML private Button addSavingsBtn;
    @FXML private Button viewGoalsBtn;
    @FXML private Button viewTransactionsBtn;

    @FXML
    private void handleAddExpense() {
        System.out.println("Add Expense clicked");
    }

    @FXML
    private void handleViewBudgets() {
        System.out.println("View Budgets clicked");
    }

    @FXML
    private void handleAddSavings() {
        System.out.println("Add Savings clicked");
    }

    @FXML
    private void handleViewGoals() {
        System.out.println("View Goals clicked");
    }

    @FXML
    private void handleViewTransactions() {
        System.out.println("View Transactions clicked");
    }
}