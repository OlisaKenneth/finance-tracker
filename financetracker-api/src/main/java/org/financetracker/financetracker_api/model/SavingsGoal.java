package org.financetracker.financetracker_api.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore; //<- stops the full User object from being sent back in every savings goal response

// This class represents a Savings Goal
// Think of it like a blueprint for what a savings goal looks like
// Every savings goal has an id, goalName, targetAmount, savedSoFar, months, and an owner (user)
@Entity // tells Spring Boot "this class is a database table"
@Table(name = "savings_goal") // the table in the database will be called "savings_goal"
public class SavingsGoal {

    @Id // this field is the unique identifier for each goal (like a student ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database automatically assigns the next number (1, 2, 3...)
    private Long id;

    // goalName cannot be empty — what are you saving for?
    @NotBlank(message = "Goal name cannot be empty")
    private String goalName;

    /*
     * @Positive means must be greater than 0
     * blocks: -8000, 0
     * allows: 0.01, 8000, 50000
     */
    @Positive(message = "Target amount must be greater than 0")
    private double targetAmount;

    // savedSoFar is not validated here because:
    // - the user never sends savedSoFar directly
    // - it is always set to 0 when a goal is created
    // - it gets updated internally via addSavings
    private double savedSoFar;

    /*
     * @Positive means must be greater than 0
     * blocks: -12, 0
     * allows: 1, 6, 24
     */
    @Positive(message = "Months must be greater than 0")
    private int months;

    /*
     * THE OWNERSHIP LINK — same pattern as Budget.java and Transaction.java.
     *
     * @ManyToOne: MANY savings goals can belong to ONE user.
     * @JoinColumn: stores this as a "user_id" column in the
     *              savings_goal table.
     * @JsonIgnore: keeps the full User object (including the
     *              hashed password) out of every API response.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // SETTERS — these methods let you PUT values into the fields

    // sets the id of this savings goal
    public void setId(Long id) {
        this.id = id;
    }

    // sets what you are saving for e.g. "Car"
    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    // sets the total amount you want to save e.g. 8000.0
    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    // sets how much you have saved so far e.g. 500.0
    public void setSavedSoFar(double savedSoFar) {
        this.savedSoFar = savedSoFar;
    }

    // sets how many months you are giving yourself to save
    public void setMonths(int months) {
        this.months = months;
    }

    // sets which user owns this savings goal
    public void setUser(User user) {
        this.user = user;
    }

    // GETTERS — these methods let you GET values out of the fields

    // returns the id of this savings goal
    public Long getId() {
        return id;
    }

    // returns what you are saving for e.g. "Car"
    public String getGoalName() {
        return goalName;
    }

    // returns the total amount you want to save e.g. 8000.0
    public double getTargetAmount() {
        return targetAmount;
    }

    // returns how much you have saved so far e.g. 500.0
    public double getSavedSoFar() {
        return savedSoFar;
    }

    // returns how many months you are giving yourself
    public int getMonths() {
        return months;
    }

    // returns which user owns this savings goal
    public User getUser() {
        return user;
    }
}