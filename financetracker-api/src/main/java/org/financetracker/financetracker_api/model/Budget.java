package org.financetracker.financetracker_api.model;

import jakarta.persistence.*;//<-Imports database labels. For example, @Entity turns a Java class into a database table, and @Id defines the primary key.
import jakarta.validation.constraints.*;//<-Imports specific rules you can label your data with, such as @NotNull, @Size, or @Email
import com.fasterxml.jackson.annotation.JsonIgnore; //<- stops the full User object from being sent back in every budget response

// This class represents a Budget
// Think of it like a blueprint for what a budget looks like
// Every budget has an id, category, monthlyLimit, spent amount, and an owner (user)
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

    /*
     * THE OWNERSHIP LINK — this is the new part.
     *
     * @ManyToOne means: "MANY budgets can belong to ONE user"
     * Picture it: User #3 can have many Budget rows, but each
     * Budget row points back to exactly one User.
     *
     * @JoinColumn tells JPA: "store this relationship as a
     * column called user_id in the budgets table" — that column
     * holds the number that links back to the users table.
     *
     * @JsonIgnore stops this field from being included when we
     * send a Budget back as JSON. Without it, every budget
     * response would try to include the ENTIRE user object
     * (including their hashed password) — we never want that.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

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

    // sets which user owns this budget
    public void setUser(User user) {
        this.user = user;
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

    // returns which user owns this budget
    public User getUser() {
        return user;
    }
}