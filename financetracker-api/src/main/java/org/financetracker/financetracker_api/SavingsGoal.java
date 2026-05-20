package org.financetracker.financetracker_api;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

// This class represents a Savings Goal
// Think of it like a blueprint for what a savings goal looks like
// Every savings goal has an id, goalName, targetAmount, savedSoFar and months
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
}