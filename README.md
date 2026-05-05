# Personal Finance Tracker 💰

A Java-based finance management system to track budgets, monitor expenses, and achieve savings goals.

## 🎯 Project Goal

Building a complete financial management application - starting from command-line, evolving to desktop GUI, and ultimately deploying as a web application. Currently helping me save **$8,000 for my first car!**

## ✨ Current Features

- **Savings Goal Tracking**: Set long-term goals with automatic monthly target calculation based on deadline
- **Budget Management**: Create and monitor spending budgets for different categories
- **Expense Tracking**: Record and categorize transactions
- **Over-Budget Alerts**: Real-time warnings when spending exceeds limits
- **Progress Monitoring**: Track percentage completion toward financial goals

## 🛠️ Technologies

- Java 24
- Maven
- Object-Oriented Programming (Encapsulation, Inheritance, Polymorphism)
- Collections Framework (ArrayList)
- Date/Time API (LocalDate)

## 📂 Project Structure

src/main/java/org/financetracker/
├── Transaction.java      # Income/expense transaction model
├── Budget.java           # Category budget management
├── SavingsGoal.java      # Long-term savings goal tracking
├── FinanceTracker.java   # Core application logic
└── Main.java             # CLI entry point

## 🚀 How to Run

```bash
# Clone the repository
git clone https://github.com/OlisaKenneth/finance-tracker.git

# Navigate to project
cd finance-tracker

# Open in IntelliJ IDEA or compile with Maven
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="org.financetracker.Main"
```

## 📊 Development Roadmap

### Phase 1: Command-Line Foundation (Current)
- [x] Core data models (Transaction, Budget, SavingsGoal)
- [x] Basic expense and budget tracking
- [ ] Transaction history with filtering
- [ ] Data persistence (file storage)
- [ ] CSV import for bank statements
- [ ] Spending reports and analytics

### Phase 2: Desktop Application
- [ ] JavaFX GUI interface
- [ ] Interactive charts and graphs
- [ ] Visual budget progress bars
- [ ] Notification system for alerts

### Phase 3: Web Application
- [ ] Spring Boot backend REST API
- [ ] React/Vue.js frontend
- [ ] Database integration (PostgreSQL)
- [ ] User authentication
- [ ] Cloud deployment (AWS/Heroku)

## 💡 What I'm Learning

- **OOP Design**: Creating clean, modular class structures with proper encapsulation
- **Data Structures**: Working with ArrayList for dynamic collections
- **Input Validation**: Handling edge cases and preventing null pointer exceptions
- **Date/Time Logic**: Calculating time spans and deadlines programmatically
- **Git Workflow**: Version control and professional commit practices
- **Software Evolution**: Building with scalability in mind from the start

## 🎓 Future Enhancements

- Machine learning for spending pattern analysis
- Budget recommendations based on historical data
- Multi-currency support
- Financial goal prioritization system
- Export to PDF reports

## 📝 License

MIT License - Open for learning and collaboration

---

**Status**: 🚧 Active Development | **Started**: May 2026

*Building this project to develop job-ready Java skills while solving a real personal finance challenge.*
