package org.financetracker;

public class Budget {
    private String category;
    private double monthlyLimit;
    private double spent;

    public Budget(String category, double monthlyLimit) {
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.spent = 0;

    }

    public boolean isOverBudget() {
        return spent>monthlyLimit;
    }

    public void addExpense(double amount){
        spent+=amount;
    }

    public String getCategory() {
        return category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public double getSpent() {
        return spent;
    }

    public double getRemainder() {
        return monthlyLimit-spent;
    }

    @Override
    public String toString() {
        return category + ": $" + spent + "/$" + monthlyLimit +
                " (Remaining: $" + getRemainder() + ")";
    }

}
