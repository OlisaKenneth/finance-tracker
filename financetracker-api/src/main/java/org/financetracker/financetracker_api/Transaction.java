package org.financetracker.financetracker_api;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

// This class represents a Transaction
// Think of it like a blueprint for what a transaction looks like
// Every transaction has an id, amount, category, description and date
@Entity // tells Spring Boot "this class is a database table"
@Table(name = "transactions") // the table in the database will be called "transactions"
public class Transaction {

    @Id // this field is the unique identifier for each transaction (like a receipt number)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database automatically assigns the next number (1, 2, 3...)
    private Long id;

    /*
     * @Positive means:
     * - must be greater than 0
     * - blocks: -50, 0
     * - allows: 0.01, 50, 100.50
     */
    @Positive(message = "Amount must be greater than 0")
    private double amount;

    /*
     * @NotBlank means:
     * - cannot be null (nothing sent at all)
     * - cannot be empty "" (empty string)
     * - cannot be just spaces "   "
     */
    @NotBlank(message = "Category cannot be empty")
    private String category;

    // description cannot be empty — what was the purchase for?
    @NotBlank(message = "Description cannot be empty")
    private String description;

    // date is not validated here because:
    // - the user never sends the date directly
    // - it is always set automatically to today's date in TransactionService
    private String date;

    // SETTERS — these methods let you PUT values into the fields

    // sets the id of this transaction
    public void setId(Long id) {
        this.id = id;
    }

    // sets the amount spent e.g. 50.0
    public void setAmount(double amount) {
        this.amount = amount;
    }

    // sets the category e.g. "Groceries"
    public void setCategory(String category) {
        this.category = category;
    }

    // sets what the purchase was for e.g. "milk and bread"
    public void setDescription(String description) {
        this.description = description;
    }

    // sets the date of the transaction e.g. "2026-05-19"
    public void setDate(String date) {
        this.date = date;
    }

    // GETTERS — these methods let you GET values out of the fields

    // returns the id of this transaction
    public Long getId() {
        return id;
    }

    // returns the amount spent e.g. 50.0
    public double getAmount() {
        return amount;
    }

    // returns the category e.g. "Groceries"
    public String getCategory() {
        return category;
    }

    // returns what the purchase was for e.g. "milk and bread"
    public String getDescription() {
        return description;
    }

    // returns the date of the transaction e.g. "2026-05-19"
    public String getDate() {
        return date;
    }
}