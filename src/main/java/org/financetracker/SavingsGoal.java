package org.financetracker;

public class SavingsGoal {
    private String goalName; //what you're saving for (like "Car")
    private double targetAmount; //total you need ($8,000)
    private double savedSoFar;// how much you've saved ($0 at start)
    private int months;//how long you're giving yourself (16)


    public SavingsGoal(String goalName, double targetAmount, int months){
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.months = months;
        this.savedSoFar =0;
    }

    //add money to your savings
    public void addSavings(double value){
        savedSoFar+=value;
    }

    //calculate how much to save per month ($8000 / 16 = $500)
    public double getMonthlyTarget(){
        return targetAmount/months;
    }

    //how much have you saved? (e.g., "25% complete")
    public double getProgress(){
        return (savedSoFar/targetAmount)*100;
    }

    //how much more do you need?
    public double getRemainingAmount(){
        return targetAmount-savedSoFar;
    }

    //did you hit your target?
    public boolean isGoalReached(){
        return savedSoFar>=targetAmount;
    }

    public double getTargetAmount(){
        return targetAmount;
    }

    public double getSavedSoFar(){
       return savedSoFar;
    }

    public int getMonths(){
        return months;
    }

    public String getGoalName(){
        return goalName;
    }

    @Override
    public String toString(){
        return String.format("%s: saved $%.2f/$%.2f, %.2f%% completed- %d months", goalName, savedSoFar,targetAmount,getProgress(),months);
    }
}
