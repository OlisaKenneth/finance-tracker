package org.financetracker;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;

public class DatabaseManager {
    private Path dbPath = Path.of("data/finance_tracker.db");
    private Connection connection;


    public void connect() {
        //dbPath.getParent()//just returns data
        try{
            Files.createDirectories(dbPath.getParent());
        }catch (IOException e){
            System.out.println("Could not create data folder: "+e.getMessage());
            return;
        }

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
                "   monthly_limit REAL," +
                "   category TEXT," +
                "   spent REAL" +
                ");";

        var savingsGoalTable = "CREATE TABLE IF NOT EXISTS savings_goal(" +
                "   savings_id INTEGER PRIMARY KEY," +
                "   goal_name TEXT," +
                "   target_amount REAL," +
                "   deadline INTEGER," +
                "   saved_so_far REAL" +
                ");";
        try {
            var stmt = connection.createStatement();
            stmt.execute(transactionTable);
            stmt.execute(budgetTable);
            stmt.execute(savingsGoalTable);
            System.out.println("Tables created successfully!" );
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}