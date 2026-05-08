package org.financetracker;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;
import java.util.Locale;

public class FinanceTracker {
    private SavingsGoal savingsGoal; //a field for our savings Goal
    private ArrayList<Budget> budgets;//a private field that is used to create an ever expanding list of budgets objects
    private ArrayList<Transaction> transactions;


    public FinanceTracker(){ //constructor
        this.savingsGoal=null; //we initialize the savings goal to null because we have not made a goal yet
        this.budgets = new ArrayList<>();//this is how we initalize the budgets arrayList
        this.transactions = new ArrayList<>();
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
    public void addExpense(String category, double value, Scanner scanner) {
        Budget c = searchForBudget(category);
            if (c != null) {
                c.addExpense(value);
                System.out.println("Added $" + value + " to " + category);

                System.out.print("what was the purchase for: ");
                String description = scanner.nextLine();
                LocalDate today = LocalDate.now();
                transactions.add(new Transaction(value,category,description,today.toString()));

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
                    double exceeded = Math.abs(b.getRemainder());
                    String t = String.format(b.getCategory() + ": $" + b.getSpent() + "/$" + b.getMonthlyLimit() +
                            " (EXCEEDED BY: $" + exceeded + ")");
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

    public void showAllTransactions(){
        if (transactions.isEmpty()){
            System.out.println("Error: No Transactions Yet");
        }else{
            System.out.println("==ALL TRANSACTIONS==");
            for (Transaction t: transactions){
                System.out.println(t);
            }
        }
    }


    public void showMenu(){
        System.out.println("=== Finance Tracker ===");
        System.out.println("1. Add Expense");
        System.out.println("2. View Budgets");
        System.out.println("3. Check Budget Alerts");
        System.out.println("4. Add Savings");
        System.out.println("5. View Savings Progress");
        System.out.println("6. Show All Transactions");
        System.out.println("7. Exit");
    }

}
