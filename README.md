# Expense Claim Management System

A full-stack Expense Claim Management System built using **Java Spring Boot, Angular, Spring Security, JWT Authentication, and PostgreSQL**.

The application allows employees to submit and manage expense claims, upload supporting receipts, and track claim statuses. Managers can review employee claims, approve or reject submissions, and monitor expenses through a dedicated dashboard.

---

## 1. Technology Stack

### Backend
- Java 21
- Spring Boot 4.1.2
- Spring Security
- Spring Data JPA / Hibernate
- JWT Authentication (JJWT 0.12.6)
- Maven
- PostgreSQL 18.4
- Lombok
- Bean Validation

### Frontend
- Angular 22
- TypeScript
- HTML5
- CSS3
- RxJS
- Angular Router
- Angular HTTP Client

### Development Tools
- IntelliJ IDEA
- Visual Studio Code
- DBeaver
- Postman
- Git & GitHub

---

## 2. Key Features

### Authentication and Authorization
- User registration and login.
- JWT-based authentication.
- Access token and refresh token support.
- Password encryption using BCrypt.
- Role-based access control.
- Employee and Manager roles.
- New registrations receive the Employee role.
- Inactive accounts are prevented from authenticating.

### Employee Module
- Employee dashboard.
- View personal expense claims.
- Submit new expense claims.
- Update pending claims.
- Delete pending claims.
- View claim status.
- Filter claims by status.
- Pagination support.
- View rejection comments.

### Manager Module
- Manager dashboard.
- View all employee expense claims.
- Search claims by employee, title, or category.
- Filter claims by status.
- Filter claims by expense date range.
- Approve pending claims.
- Reject pending claims with mandatory comments.
- View employee information associated with claims.

### Additional Features
- Receipt upload and download endpoints.
- Claim audit history.
- Employee and manager dashboards.
- Dashboard expense summaries.
- Backend exception handling.
- DTO-based request and response handling.
- Input validation.

---

## 3. Application Architecture

The application follows a layered architecture.

### Backend Architecture

```text
com.expense
│
├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── enums
├── exception
├── repository
├── security
├── service
│   └── impl
└── ExpenseManagementApplication.java
```

### Frontend Architecture

```text
src/app
│
├── core
│   ├── interceptors
│   └── services
│
├── features
│   ├── auth
│   ├── employee
│   │   ├── dashboard
│   │   └── claims
│   │
│   └── manager
│       ├── dashboard
│       └── claims
│
├── shared
│   └── models
│
├── app.config.ts
├── app.routes.ts
└── app.ts
```

### Request Flow

```text
Angular Frontend
       |
       v
HTTP Interceptor
       |
       v
JWT Authentication
       |
       v
Spring Boot Controller
       |
       v
Service Layer
       |
       v
Repository Layer
       |
       v
PostgreSQL Database
```

---

## 4. Prerequisites

Install the following before running the application:

| Software | Version |
|---|---|
| Java JDK | 21 |
| Node.js | 24 or compatible |
| npm | 11 or compatible |
| PostgreSQL | 18 or compatible |
| Maven | 3.9+ recommended |
| Angular CLI | 22 |

---

## 5. Database Setup

1. Install and start PostgreSQL.
2. Create a database named `expense_db`.

```sql
CREATE DATABASE expense_db;
```

3. Configure database credentials in:

`expense-management/src/main/resources/application.properties`

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8080
```

Replace the username and password with your local PostgreSQL credentials.

The application uses Hibernate/JPA for database persistence.

---

## 6. Backend Setup

Clone the repository:

```bash
git clone https://github.com/ayush1360/expense-claim-management-system.git
```

Navigate to the backend:

```bash
cd expense-claim-management-system/expense-management
```

Build the project:

```bash
mvn clean install
```

Run the Spring Boot application:

```bash
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

---

## 7. Frontend Setup

Open another terminal and navigate to:

```bash
cd expense-claim-management-system/expense-management-ui
```

Install dependencies:

```bash
npm install
```

Start the Angular application:

```bash
npm start
```

Alternatively:

```bash
npx ng serve
```

Frontend URL:

```text
http://localhost:4200
```

---

## 8. Sample Login Credentials

### Manager Account

| Field | Value |
|---|---|
| Email | manager@test.com |
| Password | Manager@123 |
| Role | MANAGER |

### Employee Account

| Field | Value |
|---|---|
| Email | employee@test.com |
| Password | Test@12345 |
| Role | EMPLOYEE |

You can also register a new employee using the registration screen.

**Note:** New registrations are assigned the EMPLOYEE role by default.

---

## 9. REST API Documentation

Base URL:

```text
http://localhost:8080
```

### Authentication APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new employee |
| POST | `/api/auth/login` | Authenticate user |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Logout and revoke refresh token |

### Expense Claim APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/claims` | Create an expense claim |
| GET | `/api/claims/my` | Get employee's own claims |
| GET | `/api/claims` | Get all claims |
| GET | `/api/claims/{id}` | Get claim details |
| PUT | `/api/claims/{id}` | Update a pending claim |
| DELETE | `/api/claims/{id}` | Delete a pending claim |
| PATCH | `/api/claims/{id}/review` | Approve or reject a claim |
| GET | `/api/claims/search` | Search and filter claims |

### Receipt APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/claims/{claimId}/receipt` | Upload receipt |
| GET | `/api/claims/{claimId}/receipt` | Download receipt |

Receipt upload uses multipart form data with the field name `file`.

### Dashboard APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard/employee` | Employee dashboard statistics |
| GET | `/api/dashboard/manager` | Manager dashboard statistics |

### Audit APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/claims/{claimId}/history` | Retrieve claim audit history |

**Authorization:** Protected endpoints require a valid JWT access token in the Authorization header.

```http
Authorization: Bearer YOUR_ACCESS_TOKEN
```

---

## 10. Expense Claim Workflow

```text
Employee Login
      |
      v
Create Expense Claim
      |
      v
Claim Status: PENDING
      |
      v
Manager Reviews Claim
      |
      +-------------------+
      |                   |
      v                   v
   APPROVED             REJECTED
                          |
                          v
                 Rejection Comment
```

Business rules:

- Employees can manage only their own claims.
- Employees can edit or delete only pending claims.
- Managers can review pending claims.
- A rejection comment is mandatory when rejecting a claim.
- Approved and rejected claims cannot be reviewed again.
- Role-based authorization protects restricted operations.

---

## 11. Database Entities

The application uses the following primary entities:

| Entity | Purpose |
|---|---|
| User | Stores employee and manager information |
| ExpenseClaim | Stores submitted expense claims |
| Receipt | Stores receipt metadata |
| RefreshToken | Stores refresh token information |
| ClaimAuditLog | Stores claim activity history |

### Claim Status

- PENDING
- APPROVED
- REJECTED

### User Roles

- EMPLOYEE
- MANAGER

---

## 12. Screenshots

Add your application screenshots to a folder named `screenshots` in the repository root.

Recommended folder structure:

```text
expense-claim-management-system/
│
├── expense-management/
├── expense-management-ui/
├── screenshots/
│   ├── login.png
│   ├── employee-dashboard.png
│   ├── employee-claims.png
│   ├── create-claim.png
│   ├── manager-dashboard.png
│   └── manager-claims.png
│
└── README.md
```

### Login Screen

![Login Screen](screenshots/login.png)

### Employee Dashboard

![Employee Dashboard](screenshots/employee-dashboard.png)

### Employee Claims

![Employee Claims](screenshots/employee-claims.png)

### Create Expense Claim

![Create Claim](screenshots/create-claim.png)

### Manager Dashboard

![Manager Dashboard](screenshots/manager-dashboard.png)

### Manager Claims

![Manager Claims](screenshots/manager-claims.png)

*Replace these image paths with your actual screenshots. GitHub will display them automatically once the files are committed.*

---

## 13. Security Considerations

- Passwords are encrypted using BCrypt.
- JWT authentication protects secured endpoints.
- Role-based authorization separates employee and manager operations.
- Employee claim ownership is validated on the backend.
- Request validation is handled using Jakarta Bean Validation.
- Refresh tokens are persisted and can be revoked during logout.
- Inactive users are prevented from logging in.

---

## 14. Testing

Backend tests can be executed using:

```bash
mvn test
```

The project includes service-level unit tests for expense claim business logic.

---

## 15. Future Enhancements

- Complete Manager User Management interface.
- Employee activation and deactivation management.
- Complete receipt upload and download verification.
- Enhanced audit history visualization.
- CSV export for expense reports.
- Swagger/OpenAPI documentation.
- Docker Compose deployment.
- Production deployment and environment-based configuration.

---

## 16. Author

**Ayush Singh**

GitHub: [ayush1360](https://github.com/ayush1360)

Project Repository:

[Expense Claim Management System](https://github.com/ayush1360/expense-claim-management-system)

---

## 17. License

This project was developed as a technical assignment demonstrating full-stack application development using Java Spring Boot, Angular, and PostgreSQL.
