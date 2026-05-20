package org.financetracker.financetracker_api;
import jakarta.persistence.*;

// This class represents a Savings Goal
// Think of it like a blueprint for what a savings goal looks like
// Every savings goal has an id, goalName, targetAmount, savedSoFar and months
@Entity // tells Spring Boot "this class is a database table"
@Table(name = "savings_goal") // the table in the database will be called "savings_goal"
public class SavingsGoal {

    @Id // this field is the unique identifier for each goal (like a student ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database automatically assigns the next number (1, 2, 3...)
    private Long id;

    private String goalName;    // what you are saving for e.g. "Car"
    private double targetAmount; // the total amount you want to save e.g. 8000.0
    private double savedSoFar;  // how much you have saved so far e.g. 500.0
    private int months;         // how many months you are giving yourself to save e.g. 24

    // SETTERS — these methods let you PUT values into the fields
    // like filling in a form

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
    // like reading what is written on the form

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