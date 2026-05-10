package org.financetracker;
import java.time.LocalDate;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        db.connect();
        db.createTables();


        Scanner scanner = new Scanner(System.in);
        LocalDate today = LocalDate.now();//today = 2026-05-02
        FinanceTracker tracker = new FinanceTracker(db);
        tracker.loadBudgets(db.loadBudgets());
        tracker.loadTransactions(db.loadTransactions());




        int month = today.getMonthValue();//get the month value
        int year = today.getYear();//get the year value


        System.out.print("what is your year deadline(e.g., 2028): ");
        int targetYear = scanner.nextInt();

        System.out.print("what is your month deadline(e.g., (1-12) 04 for april): ");
        int targetMonth = scanner.nextInt();

        int deadline = ((targetYear-year)*12)+(targetMonth-month);
        tracker.setSavingsGoal("Car", 8000, deadline);




//        int optionSelected;
        boolean keepGoing= true;
        while (keepGoing){
            tracker.showMenu();
            System.out.print("please select an option (0-7) > ");
            int optionSelected = scanner.nextInt();

            switch(optionSelected){
                case(0):
                    scanner.nextLine();
                    System.out.print("what category is this budget for(e.g entertainent, Groceries etc. : ");
                    String budgetCategory = scanner.nextLine();
                    System.out.print("what would be the monthly limit: ");
                    double budgetValue = scanner.nextDouble();
                    tracker.createBudget(budgetCategory,budgetValue);
                    scanner.nextLine();
                    System.out.println();
                    break;
                case(1):
                    scanner.nextLine();
                    System.out.print("what category: ");
                    String category = scanner.nextLine();
                    System.out.print("what amount did you spend: ");
                    double value = scanner.nextDouble();
                    scanner.nextLine();
                    tracker.addExpense(category,value,scanner);
                    System.out.println();
                    break;

                case(2):
                    tracker.showStatus();
                    System.out.println();
                    break;

                case(3):
                    tracker.exceededBudget();
                    System.out.println();
                    break;

                case(4):
                    System.out.print("how much money do you want to add your savings: ");
                    double amount = scanner.nextDouble();
                    tracker.addMoneyToSavings(amount);
                    System.out.println();
                    break;

                case(5):
                    System.out.println(tracker.getSavingsGoal());
                    System.out.println();
                    break;

                case(6):
                    tracker.showAllTransactions();
                    System.out.println();
                    break;

                case(7):
                    System.out.println("GOODBYE");
                    keepGoing=false;
                    System.out.println();
                    break;

                default:
                    System.out.println("Invalid option! Try again.");
                    break;

            }

        }



    }
}

