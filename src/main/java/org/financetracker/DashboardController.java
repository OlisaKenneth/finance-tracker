package org.financetracker;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

public class DashboardController {

    private DatabaseManager db;
    private FinanceTracker tracker;

    public DashboardController() {
        db = new DatabaseManager();
        db.connect();
        db.createTables();
        tracker = new FinanceTracker(db);
        tracker.loadBudgets(db.loadBudgets());
        tracker.loadTransactions(db.loadTransactions());
        tracker.loadSavingsGoals(db.loadSavingsGoals());
    }

    @FXML
    private void handleCreateBudget() {
        TextInputDialog categoryDialog = new TextInputDialog();
        categoryDialog.setTitle("Create Budget");
        categoryDialog.setHeaderText("New Budget");
        categoryDialog.setContentText("Category (e.g. Groceries):");
        Optional<String> category = categoryDialog.showAndWait();

        if (category.isPresent() && !category.get().isEmpty()) {
            TextInputDialog limitDialog = new TextInputDialog();
            limitDialog.setTitle("Create Budget");
            limitDialog.setHeaderText("Monthly Limit");
            limitDialog.setContentText("Monthly limit ($):");
            Optional<String> limit = limitDialog.showAndWait();

            if (limit.isPresent()) {
                try {
                    double monthlyLimit = Double.parseDouble(limit.get());
                    tracker.createBudget(category.get(), monthlyLimit);
                    showInfo("Budget Created", "Budget for " + category.get() + " created successfully!");
                } catch (NumberFormatException e) {
                    showError("Invalid amount entered.");
                }
            }
        }
    }

    @FXML
    private void handleViewBudgets() {
        StringBuilder sb = new StringBuilder();
        if (tracker.getBudgets().isEmpty()) {
            sb.append("No budgets yet.");
        } else {
            for (Budget b : tracker.getBudgets()) {
                sb.append(b.toString()).append("\n");
            }
        }
        showInfo("All Budgets", sb.toString());
    }

    @FXML
    private void handleAddExpense() {
        TextInputDialog categoryDialog = new TextInputDialog();
        categoryDialog.setTitle("Add Expense");
        categoryDialog.setHeaderText("Add Expense");
        categoryDialog.setContentText("Category:");
        Optional<String> category = categoryDialog.showAndWait();

        if (category.isPresent() && !category.get().isEmpty()) {
            TextInputDialog amountDialog = new TextInputDialog();
            amountDialog.setTitle("Add Expense");
            amountDialog.setHeaderText("Amount");
            amountDialog.setContentText("Amount ($):");
            Optional<String> amount = amountDialog.showAndWait();

            if (amount.isPresent()) {
                try {
                    double value = Double.parseDouble(amount.get());

                    TextInputDialog descDialog = new TextInputDialog();
                    descDialog.setTitle("Add Expense");
                    descDialog.setHeaderText("Description");
                    descDialog.setContentText("What was this purchase for:");
                    Optional<String> desc = descDialog.showAndWait();

                    if (desc.isPresent()) {
                        tracker.addExpenseGUI(category.get(), value, desc.get());
                        showInfo("Expense Added", "$" + value + " added to " + category.get());
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid amount entered.");
                }
            }
        }
    }

    @FXML
    private void handleSetSavingsGoal() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Savings Goal");
        nameDialog.setHeaderText("New Savings Goal");
        nameDialog.setContentText("What are you saving for:");
        Optional<String> name = nameDialog.showAndWait();

        if (name.isPresent() && !name.get().isEmpty()) {
            TextInputDialog amountDialog = new TextInputDialog();
            amountDialog.setTitle("Savings Goal");
            amountDialog.setHeaderText("Target Amount");
            amountDialog.setContentText("Target amount ($):");
            Optional<String> amount = amountDialog.showAndWait();

            if (amount.isPresent()) {
                try {
                    double target = Double.parseDouble(amount.get());

                    TextInputDialog monthsDialog = new TextInputDialog();
                    monthsDialog.setTitle("Savings Goal");
                    monthsDialog.setHeaderText("Timeline");
                    monthsDialog.setContentText("How many months to save:");
                    Optional<String> months = monthsDialog.showAndWait();

                    if (months.isPresent()) {
                        int numMonths = Integer.parseInt(months.get());
                        tracker.setSavingsGoal(name.get(), target, numMonths);
                        showInfo("Goal Set", "Savings goal for " + name.get() + " created!");
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid number entered.");
                }
            }
        }
    }

    @FXML
    private void handleAddSavings() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Add Savings");
        nameDialog.setHeaderText("Add To Savings");
        nameDialog.setContentText("Which savings goal:");
        Optional<String> name = nameDialog.showAndWait();

        if (name.isPresent() && !name.get().isEmpty()) {
            TextInputDialog amountDialog = new TextInputDialog();
            amountDialog.setTitle("Add Savings");
            amountDialog.setHeaderText("Amount");
            amountDialog.setContentText("Amount to add ($):");
            Optional<String> amount = amountDialog.showAndWait();

            if (amount.isPresent()) {
                try {
                    double value = Double.parseDouble(amount.get());
                    tracker.addMoneyToSavings(name.get(), value);
                    showInfo("Savings Added", "$" + value + " added to " + name.get());
                } catch (NumberFormatException e) {
                    showError("Invalid amount entered.");
                }
            }
        }
    }

    @FXML
    private void handleViewGoals() {
        StringBuilder sb = new StringBuilder();
        if (tracker.getSavingsGoals().isEmpty()) {
            sb.append("No savings goals yet.");
        } else {
            for (SavingsGoal g : tracker.getSavingsGoals()) {
                sb.append(g.toString()).append("\n");
            }
        }
        showInfo("All Savings Goals", sb.toString());
    }

    @FXML
    private void handleViewTransactions() {
        StringBuilder sb = new StringBuilder();
        if (tracker.getTransactions().isEmpty()) {
            sb.append("No transactions yet.");
        } else {
            for (Transaction t : tracker.getTransactions()) {
                sb.append(t.toString()).append("\n");
            }
        }
        showInfo("All Transactions", sb.toString());
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}