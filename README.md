# Finance Tracker API

A production-grade RESTful API for personal finance management built with Java 21 and Spring Boot 3.5. Users can create and manage budgets, log transactions against those budgets, and track progress toward savings goals. Transactions automatically update the spent amount on the matching budget in real time.

Deployed on Railway with a managed PostgreSQL database and continuous deployment via GitHub webhooks. A React frontend consuming this API is live at [finance-tracker-frontend-six-rho.vercel.app](https://finance-tracker-frontend-six-rho.vercel.app).

---

## Live

| Resource | URL |
|----------|-----|
| API Base | https://finance-tracker-production-1547.up.railway.app |
| Budgets Endpoint | https://finance-tracker-production-1547.up.railway.app/api/budgets |
| Frontend | https://finance-tracker-frontend-six-rho.vercel.app |
| Frontend Repo | https://github.com/OlisaKenneth/finance-tracker-frontend |

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 21 | Core language |
| Spring Boot 3.5 | Web framework and application container |
| Spring Data JPA | Database abstraction layer |
| Hibernate | ORM — maps Java classes to database tables |
| PostgreSQL | Production database (Railway) |
| SQLite | Local development database (no setup required) |
| Maven | Build tool and dependency management |
| Docker | Containerization for portable local development |
| Railway | Cloud deployment and managed PostgreSQL |

---

## Architecture

This API follows a strict layered architecture a standard pattern in enterprise Java development:

```
HTTP Request
     │
     ▼
┌─────────────────────────────┐
│         Controller          │  Receives HTTP requests, validates input,
│  BudgetController.java      │  returns HTTP responses. No business logic.
│  TransactionController.java │
│  SavingsGoalController.java │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│           Service           │  Contains all business logic.
│  BudgetService.java         │  Decides what to do with the data.
│  TransactionService.java    │  Example: when a transaction is created,
│  SavingsGoalService.java    │  the service finds the matching budget
└─────────────┬───────────────┘  and updates its spent amount.
              │
              ▼
┌─────────────────────────────┐
│         Repository          │  Talks directly to the database.
│  BudgetRepository.java      │  Extends JpaRepository — gets free
│  TransactionRepository.java │  SELECT, INSERT, UPDATE, DELETE without
│  SavingsGoalRepository.java │  writing any SQL.
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│         PostgreSQL          │  Persistent data storage.
│  budgets table              │  Tables are auto-created by Hibernate
│  transactions table         │  from the Java entity classes on startup.
│  savings_goal table         │
└─────────────────────────────┘
```

**Supporting components:**

- **GlobalExceptionHandler**  catches all validation errors, database conflicts, and unexpected exceptions across every controller and returns clean structured JSON error responses instead of Spring Boot's default verbose error pages.
- **WebConfig**  configures CORS to allow the React frontend to make cross-origin requests to the API. Explicitly allows GET, POST, PUT, and DELETE methods from the frontend origin.

---

## Database Schema

```sql
CREATE TABLE budgets (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    category      TEXT UNIQUE NOT NULL,
    monthly_limit REAL NOT NULL,
    spent         REAL DEFAULT 0
);

CREATE TABLE transactions (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    amount      REAL NOT NULL,
    category    TEXT NOT NULL,
    description TEXT NOT NULL,
    date        TEXT NOT NULL
);

CREATE TABLE savings_goal (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    goal_name     TEXT NOT NULL,
    target_amount REAL NOT NULL,
    saved_so_far  REAL DEFAULT 0,
    months        INTEGER NOT NULL
);
```

**Key relationships:**
- Transactions reference a budget category (string match, not a foreign key yet foreign keys with full relational linking are planned for P2)
- When a transaction is created, `TransactionService` looks up the matching budget by category and adds the transaction amount to `budget.spent` automatically

---

## API Endpoints

### Budgets = `/api/budgets`

#### GET /api/budgets
Returns all budgets as a JSON array.

**Response 200:**
```json
[
  {
    "id": 1,
    "category": "Groceries",
    "monthlyLimit": 500.0,
    "spent": 120.0
  }
]
```

---

#### POST /api/budgets
Creates a new budget. Validates all fields before saving.

**Request body:**
```json
{
  "category": "Groceries",
  "monthlyLimit": 500.0
}
```

**Validation rules:**
- `category`  cannot be null, empty, or whitespace only
- `monthlyLimit`  must be greater than 0

**Response 200**  returns the created budget with its assigned id and `spent` initialized to 0:
```json
{
  "id": 1,
  "category": "Groceries",
  "monthlyLimit": 500.0,
  "spent": 0.0
}
```

**Response 400**  validation failure:
```json
{
  "errors": [
    "Category cannot be empty",
    "Monthly limit must be greater than 0"
  ]
}
```

**Response 409**  duplicate category:
```json
{
  "error": "This data already exists or violates a database rule"
}
```

---

#### PUT /api/budgets/{id}
Updates the category and monthly limit of an existing budget. Does not reset spent.

**Request body:**
```json
{
  "category": "Groceries",
  "monthlyLimit": 600.0
}
```

**Response 200**  returns the updated budget
**Response 404**  budget with that id does not exist

---

#### DELETE /api/budgets/{id}
Permanently deletes a budget by id.

**Response 200**  deleted successfully
**Response 404**  budget with that id does not exist

---

### Transactions — `/api/transactions`

#### GET /api/transactions
Returns all transactions as a JSON array, ordered by insertion.

**Response 200:**
```json
[
  {
    "id": 1,
    "amount": 50.0,
    "category": "Groceries",
    "description": "Weekly groceries run",
    "date": "2026-06-12"
  }
]
```

---

#### POST /api/transactions
Records a new transaction and automatically updates the matching budget's spent amount.

**Request body:**
```json
{
  "amount": 50.0,
  "category": "Groceries",
  "description": "Weekly groceries run"
}
```

**Validation rules:**
- `amount`  must be greater than 0
- `category`  cannot be null, empty, or whitespace only
- `description`  cannot be null, empty, or whitespace only
- `date` is set automatically to today's date by the server users do not send it

**What happens internally:**
1. Transaction is validated
2. Transaction is saved to the transactions table
3. `TransactionService` searches for a budget with a matching category
4. If found, the transaction amount is added to `budget.spent`
5. The updated budget is saved back to the database

**Response 200**  returns the created transaction with server-assigned date:
```json
{
  "id": 1,
  "amount": 50.0,
  "category": "Groceries",
  "description": "Weekly groceries run",
  "date": "2026-06-12"
}
```

---

#### DELETE /api/transactions/{id}
Permanently deletes a transaction by id.

**Response 200**  deleted successfully
**Response 404**  transaction with that id does not exist

---

### Savings Goals = `/api/savings_goal`

#### GET /api/savings_goal
Returns all savings goals.

**Response 200:**
```json
[
  {
    "id": 1,
    "goalName": "Car",
    "targetAmount": 8000.0,
    "savedSoFar": 500.0,
    "months": 24
  }
]
```

---

#### POST /api/savings_goal
Creates a new savings goal.

**Request body:**
```json
{
  "goalName": "Car",
  "targetAmount": 8000.0,
  "months": 24
}
```

**Validation rules:**
- `goalName`  cannot be null, empty, or whitespace only
- `targetAmount`  must be greater than 0
- `months`  must be greater than 0
- `savedSoFar` is initialized to 0 by the server automatically

**Response 200**  returns the created savings goal:
```json
{
  "id": 1,
  "goalName": "Car",
  "targetAmount": 8000.0,
  "savedSoFar": 0.0,
  "months": 24
}
```

---

#### PUT /api/savings_goal/{goalName}/add?value=500
Adds money to an existing savings goal by name.

**URL parameters:**
- `goalName`  the name of the goal (path variable)
- `value`  the amount to add (query parameter)

**Example:**
```
PUT /api/savings_goal/Car/add?value=500
```

**What happens internally:**
1. Finds the goal by name
2. Adds the value to `savedSoFar`
3. Saves and returns the updated goal

**Response 200** returns the updated goal
**Response 404** no goal with that name exists

---

## Error Handling

All errors across every endpoint are handled by `GlobalExceptionHandler` and return consistent JSON:

| Scenario | Status Code | Response |
|----------|-------------|----------|
| Validation failure | 400 Bad Request | `{"errors": ["..."]}` |
| Resource not found | 404 Not Found | `{"error": "..."}` |
| Duplicate data | 409 Conflict | `{"error": "..."}` |
| Unknown URL | 404 Not Found | `{"error": "The URL you requested does not exist"}` |
| Unexpected server error | 500 Internal Server Error | `{"error": "Something went wrong on the server"}` |

---

## Environment Configuration

The app switches databases automatically based on environment:

```
Local development  → SQLite (file-based, no setup needed)
Production Railway → PostgreSQL (managed, persistent)
```

Spring Boot reads the `DATABASE_URL` environment variable on Railway. The URL is stored securely as a Railway environment variable and never hardcoded in the codebase.

```properties
# application.properties
spring.datasource.url=${DATABASE_URL}
```

---

## Run Locally

```bash
# Clone the repo
git clone https://github.com/OlisaKenneth/finance-tracker.git

# Navigate to the API
cd finance-tracker/financetracker-api

# Run with Maven
mvn spring-boot:run
```

App runs on `http://localhost:8080`. Uses SQLite locally — no database setup needed.

---

## Run with Docker

```bash
docker-compose up --build
```

Spins up the Spring Boot app and a local PostgreSQL container together.

---

## Project Structure

```
financetracker-api/
└── src/main/java/org/financetracker/financetracker_api/
    ├── Budget.java                    — entity (maps to budgets table)
    ├── BudgetController.java          — GET, POST, PUT, DELETE /api/budgets
    ├── BudgetService.java             — budget business logic
    ├── BudgetRepository.java          — database operations for budgets
    ├── Transaction.java               — entity (maps to transactions table)
    ├── TransactionController.java     — GET, POST, DELETE /api/transactions
    ├── TransactionService.java        — transaction logic + budget spent update
    ├── TransactionRepository.java     — database operations for transactions
    ├── SavingsGoal.java               — entity (maps to savings_goal table)
    ├── SavingsGoalController.java     — GET, POST, PUT /api/savings_goal
    ├── SavingsGoalService.java        — savings goal business logic
    ├── SavingsGoalRepository.java     — database operations for savings goals
    ├── GlobalExceptionHandler.java    — centralized error handling
    └── WebConfig.java                 — CORS configuration
```

---

## Roadmap

- [x] Phase 1 — Command-line Java app with SQLite
- [x] Phase 2 — JavaFX desktop GUI
- [x] Phase 3 — Spring Boot REST API deployed on Railway with PostgreSQL
- [x] Phase 4 — React frontend deployed on Vercel
- [ ] Phase 5 — JWT authentication (user registration, login, protected routes)
- [ ] Phase 6 — E-Commerce API (products, orders, role-based access control)
- [ ] Phase 7 — Social Media API (posts, likes, follows, file uploads, pagination)
- [ ] Phase 8 — Bank account syncing via Flinks (real Canadian bank transaction import)
- [ ] Phase 9 — AI transaction categorization via Anthropic Claude API
- [ ] Phase 10 — Full Mint/YNAB-style app with multi-user support

---

## Developer

Kenneth Olisa
GitHub: [OlisaKenneth](https://github.com/OlisaKenneth)
Portfolio: [olisakenneth.netlify.app](https://olisakenneth.netlify.app)