package org.financetracker;
import java.util.ArrayList;
import java.util.Locale;

public class FinanceTracker {
    private SavingsGoal savingsGoal; //a field for our savings Goal
    private ArrayList<Budget> budgets;//a private field that is used to create an ever expanding list of budgets objects


    public FinanceTracker(){ //constructor
        this.savingsGoal=null; //we initialize the savings goal to null because we have not made a goal yet
        this.budgets = new ArrayList<>();//this is how we initalize the budgets arrayList
    }


//initialize the savings goal object
//it should have a goalName, a target amount and a number of months you want to save
    public void setSavingsGoal(String goalName, double targetAmount, int months){
        savingsGoal = new SavingsGoal(goalName, targetAmount, months);
    }

//initialize the budget by giving it, its category, and a monthly limit
    public void createBudget(String category,double monthlyLimit){
        budgets.add(new Budget(category, monthlyLimit));
    }

//this is to give the user the savings Goal
    public SavingsGoal getSavingsGoal(){
        return savingsGoal;
    }

//this is to add money to savedSoFar variable in the SavingsGoal.java

    public void addMoney(double value){
        if(savingsGoal!=null){
            savingsGoal.addSavings(value);
        }else{
            System.out.println("Error no object initialized yet");
        }
    }

////this shows us how much we have saved so far
//    public String savingProgress(){
//        return savingsGoal.toString();
//    }

//this is where we can search for the budget via its category
    public Budget searchForBudget(String category){
        for (int i=0; i<budgets.size(); i++){
            Budget b = budgets.get(i);
            if (b.getCategory().equalsIgnoreCase(category)) {
                return b;

            }
        }
        return null;
    }

//we have a method to add an expense after searching for the particular category
    public void addExpense(String category, double value) {
        Budget c = searchForBudget(category);
            if (c != null) {
                c.addExpense(value);
                System.out.println("Added $" + value + " to " + category);
            }
            else{
                System.out.println("no such budget exists for category: "+category);
            }
    }

//show all the status of all budget
    public void showStatus(){
        if (budgets.isEmpty()){
            System.out.println("no budgets added>");
        }
        else {
            System.out.println("==ALL BUDGETS==");
            for (Budget b : budgets) {
                if (b.getRemainder()<0){
                    String t = String.format(b.getCategory() + ": $" + b.getSpent() + "/$" + b.getMonthlyLimit() +
                            " (EXCEEDED BY: $" + b.getRemainder() + ")");
                    System.out.println(t);
                }else{
                     System.out.println(b);}

            }
        }
    }


//this is to see if we have exceeded budget
    public void exceededBudget(){
        for (Budget b: budgets){
            if (b.isOverBudget()){
                System.out.println("⚠️ You have exceeded budget for: " +b.getCategory());
            }
        }
    }

}
