package org.financetracker;
import java.time.LocalDate;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LocalDate today = LocalDate.now();//today = 2026-05-02


        int month = today.getMonthValue();//get the month value
        int year = today.getYear();//get the year value


        System.out.print("what is your year deadline(e.g., 2028): ");
        int targetYear = scanner.nextInt();

        System.out.print("what is your month deadline(e.g., (1-12) 04 for april): ");
        int targetMonth = scanner.nextInt();

        int deadline = ((targetYear-year)*12)+(targetMonth-month);
//        System.out.printf("time needed to save the amount: %d%n", deadline);




        FinanceTracker tracker = new FinanceTracker();
        tracker.setSavingsGoal("Car", 8000, deadline);
        tracker.createBudget("Entertainment", 300);
        tracker.createBudget("Groceries", 150);



        tracker.addMoney(500);
        System.out.println("savings goal: "+ tracker.getSavingsGoal());


        System.out.println(tracker.searchForBudget("Groceries"));
        tracker.addExpense("Groceries", 100);
        tracker.addExpense("Groceries", 50);   // Capital G
        tracker.addExpense("groceries", 30);   // Lowercase g
        tracker.addExpense("GROCERIES", 20);   // All caps
        tracker.addExpense("GrOcErIeS", 10);   // Mixed case

        tracker.showStatus();
        tracker.exceededBudget();
    }
}

