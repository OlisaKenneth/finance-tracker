# Finance Tracker API 💰

A RESTful API for personal finance management — track budgets, log expenses, and work toward savings goals.

Built with Java and Spring Boot, deployed on Railway with PostgreSQL.

## 🌐 Live API

```
https://finance-tracker-production-1547.up.railway.app
```

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.5**
- **Spring Data JPA / Hibernate**
- **PostgreSQL** (production)
- **SQLite** (local development)
- **Maven**
- **Railway** (deployment)

## 📡 API Endpoints

### Budgets
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/budgets | Get all budgets |
| POST | /api/budgets | Create a new budget |
| PUT | /api/budgets/{id} | Update a budget |
| DELETE | /api/budgets/{id} | Delete a budget |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/transactions | Get all transactions |
| POST | /api/transactions | Record a new transaction |

### Savings Goals
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/savings_goal | Get all savings goals |
| POST | /api/savings_goal | Create a savings goal |
| PUT | /api/savings_goal/{goalName}/add?value=X | Add money to a goal |

## 📋 Example Requests

**Create a budget:**
```json
POST /api/budgets
{
    "category": "Groceries",
    "monthlyLimit": 500.0
}
```

**Record a transaction:**
```json
POST /api/transactions
{
    "amount": 50.0,
    "category": "Groceries",
    "description": "Weekly groceries"
}
```

**Create a savings goal:**
```json
POST /api/savings_goal
{
    "goalName": "Car",
    "targetAmount": 8000.0,
    "months": 24
}
```

**Add money to savings:**
```
PUT /api/savings_goal/Car/add?value=500
```

## ✅ Validation

All endpoints validate input and return clear error messages:

```json
{
    "errors": [
        "Category cannot be empty",
        "Monthly limit must be greater than 0"
    ]
}
```

## 🏗️ Architecture

```
Controller → Service → Repository → PostgreSQL
```

- **Controller** — handles HTTP requests and responses
- **Service** — contains business logic
- **Repository** — talks to the database via JPA
- **GlobalExceptionHandler** — handles all errors cleanly

## 🚀 Run Locally

```bash
# Clone the repo
git clone https://github.com/OlisaKenneth/finance-tracker.git

# Navigate to the API folder
cd finance-tracker/financetracker-api

# Run with Maven
mvn spring-boot:run
```

App runs on `http://localhost:8080`

> Locally uses SQLite. No database setup needed.

## 📦 Project Structure

```
finance-tracker/
├── src/                          # Phase 1: CLI app (Java)
└── financetracker-api/           # Phase 3: Spring Boot REST API
    └── src/main/java/
        └── org/financetracker/financetracker_api/
            ├── Budget.java
            ├── BudgetController.java
            ├── BudgetService.java
            ├── BudgetRepository.java
            ├── Transaction.java
            ├── TransactionController.java
            ├── TransactionService.java
            ├── TransactionRepository.java
            ├── SavingsGoal.java
            ├── SavingsGoalController.java
            ├── SavingsGoalService.java
            ├── SavingsGoalRepository.java
            └── GlobalExceptionHandler.java
```

## 🗺️ Roadmap

- [x] Phase 1: Command-line Java app
- [x] Phase 2: JavaFX desktop GUI
- [x] Phase 3: Spring Boot REST API (deployed ✅)
- [ ] Phase 4: React frontend
- [ ] Phase 5: User authentication (JWT)
- [ ] Phase 6: E-Commerce API

## 👨🏾‍💻 Author

**Kenneth Olisa** — Junior Backend Developer
Building job-ready Java skills one project at a time.

---

*Status: 🟢 Live | Started: May 2026*