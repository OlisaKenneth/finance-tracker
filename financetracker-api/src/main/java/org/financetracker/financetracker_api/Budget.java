package org.financetracker.financetracker_api;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

// This class represents a Budget
// Think of it like a blueprint for what a budget looks like
// Every budget has an id, category, monthlyLimit and spent amount
@Entity // tells Spring Boot "this class is a database table"
@Table(name = "budgets") // the table in the database will be called "budgets"
public class Budget {

    @Id // this field is the unique identifier for each budget (like a student ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database automatically assigns the next number (1, 2, 3...)
    private Long id;

    /*
     * @NotBlank means:
     * - cannot be null (nothing sent at all)
     * - cannot be empty "" (empty string)
     * - cannot be just spaces "   "
     * message = what the user sees if they break this rule
     */
    @NotBlank(message = "Category cannot be empty")
    private String category;

    /*
     * @Positive means:
     * - must be greater than 0
     * - blocks: -500, 0
     * - allows: 0.01, 100, 500.50
     * message = what the user sees if they break this rule
     */
    @Positive(message = "Monthly limit must be greater than 0")
    private double monthlyLimit;

    // spent is not validated here because:
    // - the user never sends spent directly
    // - it is always set to 0 when a budget is created
    // - it gets updated internally when expenses are added
    private double spent;

    // SETTERS — these methods let you PUT values into the fields
    // like filling in a form

    // sets the id of this budget
    public void setId(Long id) {
        this.id = id;
    }

    // sets what category this budget is for e.g. "Groceries"
    public void setCategory(String category) {
        this.category = category;
    }

    // sets the maximum monthly spending limit e.g. 500.0
    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    // sets how much has been spent so far
    public void setSpent(double spent) {
        this.spent = spent;
    }

    // GETTERS — these methods let you GET values out of the fields
    // like reading what is written on the form

    // returns the id of this budget
    public Long getId() {
        return id;
    }

    // returns the category e.g. "Groceries"
    public String getCategory() {
        return category;
    }

    // returns the monthly limit e.g. 500.0
    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    // returns how much has been spent so far
    public double getSpent() {
        return spent;
    }
}