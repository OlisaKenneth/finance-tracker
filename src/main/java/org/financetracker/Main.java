package org.financetracker;
import java.time.LocalDate;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LocalDate today = LocalDate.now();//today = 2026-05-02


        int month = today.getMonthValue();
        int year = today.getYear();


        System.out.print("what is your year deadline(e.g., 2028): ");
        int targetYear = scanner.nextInt();

        System.out.print("what is your month deadline(e.g., (1-12) 04 for april): ");
        int targetMonth = scanner.nextInt();

        int deadline = ((targetYear-year)*12)+(targetMonth-month);

        System.out.printf("time needed to save the amount: %d%n", deadline);

        FinanceTracker tracker = new FinanceTracker();
        tracker.setSavingsGoal("Car", 8000, deadline);

        tracker.addMoney(500);
        System.out.println(tracker.savingProgress());
    }
}

//may29th and then they come to saskatoon may 31st and they stay here till june 4th and winnipeg till june 22nd"