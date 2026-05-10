package org.financetracker;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class DatabaseManager {
    //this describes the relative path
    private Path dbPath = Path.of("data/finance_tracker.db");
    //this is how a connection is made to a database
    private Connection connection;

//this is to set up a connection to a particular database this case sqlite
    public void connect() {

        try{
            //this creates directories
            //dbPath.getParent()//just returns data
            Files.createDirectories(dbPath.getParent());
        }catch (IOException e){
            System.out.println("Could not create data folder: "+e.getMessage());
            return;
        }

        //this is how the url for sqlite is created
        var url = "jdbc:sqlite:"+dbPath;
        try{

            connection = DriverManager.getConnection(url);
            if(connection != null){
                var meta = connection.getMetaData();
                System.out.println("The driver name is "+ meta.getDriverName());
                System.out.println("A new database has been created");
            }

            System.out.println("connection established Successfully");
        }catch (SQLException e){
            System.out.println(e.getMessage());;
        }

    }

    public void createTables(){
        var transactionTable = "CREATE TABLE IF NOT EXISTS transactions(" +
                "   transaction_id INTEGER PRIMARY KEY," +
                "   category TEXT," +
                "   description TEXT," +
                "   amount REAL," +
                "   date TEXT" +
                ");";

        var budgetTable = "CREATE TABLE IF NOT EXISTS budgets(" +
                "   budget_id INTEGER PRIMARY KEY," +
                "   category TEXT UNIQUE COLLATE NOCASE," +
                "   monthly_limit REAL," +
                "   spent REAL" +
                ");";

        var savingsGoalTable = "CREATE TABLE IF NOT EXISTS savings_goal(" +
                "   savings_id INTEGER PRIMARY KEY," +
                "   goal_name TEXT UNIQUE COLLATE NOCASE," +
                "   target_amount REAL," +
                "   deadline INTEGER," +
                "   saved_so_far REAL" +
                ");";

        try {
            var stmt = connection.createStatement();
            stmt.execute(transactionTable);
            stmt.execute(budgetTable);
            stmt.execute(savingsGoalTable);
            System.out.println("Tables created successfully!");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //
    public void saveBudget(Budget budget){
        var sql = "INSERT OR IGNORE INTO budgets(category,monthly_limit,spent) VALUES(?,?,?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, budget.getCategory());
            pstmt.setDouble(2, budget.getMonthlyLimit());
            pstmt.setDouble(3,budget.getSpent());
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void saveTransaction(Transaction transaction){
        var sql = "INSERT INTO transactions(category,description,amount, date) VALUES(?,?,?,?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, transaction.getCategory());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setDouble(3,transaction.getAmount());
            pstmt.setString(4,transaction.getDate());
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void saveSavingsGoal(SavingsGoal savingsGoal){
        var sql = "INSERT OR REPLACE INTO savings_goal(goal_name,target_amount,deadline,saved_so_far) VALUES(?,?,?,?)";
        try(PreparedStatement pstmt = connection.prepareStatement(sql)){
            pstmt.setString(1, savingsGoal.getGoalName());
            pstmt.setDouble(2, savingsGoal.getTargetAmount());
            pstmt.setInt(3, savingsGoal.getMonths());
            pstmt.setDouble(4, savingsGoal.getSavedSoFar());
            pstmt.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<Budget> loadBudgets(){
        var stmt = "SELECT * FROM budgets";
        ArrayList<Budget> budgets = new ArrayList<>();
        try(Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(stmt)){

            while (rs.next()){
                Budget b = new Budget(rs.getString("category"),rs.getDouble("monthly_limit"));
                b.addExpense(rs.getDouble("spent"));
                budgets.add(b);

            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return budgets;
    }

    public ArrayList<Transaction> loadTransactions(){
        var stmt = "SELECT * FROM transactions";
        ArrayList<Transaction> transactions = new ArrayList<>();
        try(Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(stmt)){
            while(rs.next()){
                Transaction t = new Transaction(rs.getDouble("amount"), rs.getString("category"),
                        rs.getString("description"), rs.getString("date"));
                transactions.add(t);
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

        return transactions;
    }

    public void updateDbBudgetSpent(double value,String categoryy){

        var sql = "UPDATE budgets SET spent = ? "
        + " WHERE UPPER(category) = UPPER(?)";

        try(PreparedStatement pStmt= connection.prepareStatement(sql)){
            pStmt.setDouble(1, value);
            pStmt.setString(2, categoryy);
            int rowsAffected = pStmt.executeUpdate();
            System.out.println("DEBUG: updated " + rowsAffected + " rows for category: " + categoryy); // ← add this

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}