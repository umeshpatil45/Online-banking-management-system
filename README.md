# Online Banking Management System

A secure, modern, intermediate-level full-stack online banking simulation application built with **Spring Boot 3**, **Spring Security 6**, **JWT Authentication**, **Spring Data JPA**, **MySQL**, and a responsive **HTML5/CSS3/Bootstrap 5** frontend.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Database Setup](#database-setup)
6. [Backend Setup & Running](#backend-setup--running)
7. [Frontend Setup & Running](#frontend-setup--running)
8. [API Endpoints Reference](#api-endpoints-reference)
9. [Authentication & Security](#authentication--security)
10. [Testing with Postman](#testing-with-postman)
11. [Screenshots](#screenshots)
12. [Future Enhancements](#future-enhancements)
13. [Developer](#developer)

---

## Project Overview

The **Online Banking Management System** simulates real-world commercial banking workflows in an educational environment. The system enables retail customers to open bank accounts, perform authenticated logins, review account balances, execute real-time deposits and withdrawals, perform atomic inter-account fund transfers, search transaction histories, and update personal profile details. It also features a dedicated **Admin Management Portal** for system audits, account blocking/unblocking, and system-wide monitoring.

> [!NOTE]
> **Educational Simulation Notice**: This application does not integrate real financial gateways, cards, or bank networks. All financial operations are simulated in a secured environment.

---

## Features

### 1. Customer Banking Portal
- **User Registration**: Register with name, email, mobile, and password. An active bank account with a unique 10-digit account number (e.g. `1000123456`) and welcome opening balance is automatically generated.
- **Secure Authentication**: Stateless JWT-based authentication using HMAC-SHA256 and BCrypt password encryption.
- **Account Overview**: Real-time balance inquiry, masked account numbers with show/hide eye toggle, account type (SAVINGS/CURRENT), and account status.
- **Deposit Funds**: Deposit money with amount validation (> 0), account status check, and instant balance update.
- **Withdraw Funds**: Withdraw money with strict validation against available balance. Returns a clear "Insufficient balance" response if funds are inadequate.
- **Atomic Fund Transfers**: Transfer money to other registered account holders. Commits as a single `@Transactional` database transaction—preventing partial debits. Includes beneficiary account verification preview and a confirmation modal.
- **Transaction History**: Audit log tracking deposits, withdrawals, and transfers with search filtering, type filters, date filters, and one-click **CSV Statement Export**.
- **Profile Management**: Update full name, mobile number, and change passwords with current-password verification.

### 2. Administrator Module
- **Dashboard Metrics**: Summary cards displaying total users, active/blocked accounts, total transactions, and aggregate deposit/transfer volume.
- **Account Controls**: Search all bank accounts and immediately toggle **Block / Unblock** status. Blocked accounts are prohibited from executing transactions until unblocked.
- **User Directory**: View registered customers without exposing encrypted password hashes.
- **System-Wide Transaction Log**: Audit all transactions occurring across the entire platform.

---

## Technology Stack

### Backend
- **Language**: Java 17 / 21 / 25
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security 6 (Stateless JWT, BCrypt)
- **Token Handling**: JJWT 0.12.5 (HMAC-SHA256)
- **Data Access**: Spring Data JPA, Hibernate ORM
- **Build Tool**: Apache Maven 3.9+
- **Validation**: Jakarta Validation API (`@NotBlank`, `@Email`, `@DecimalMin`, etc.)

### Frontend
- **Markup & Styling**: HTML5, CSS3, Bootstrap 5.3.3, Bootstrap Icons 1.11
- **Scripting**: Modern Vanilla JavaScript (ES6+), Fetch API
- **Visuals**: Chart.js for cash flow analysis

### Database
- **Primary Database**: MySQL 8.0+
- **Embedded / Dev Profile**: H2 in-memory database (MySQL compatibility mode)

---

## Project Structure

```
online-banking-management-system/
│
├── backend/
│   ├── src/main/java/com/banking/
│   │   ├── OnlineBankingApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── DataInitializer.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   ├── AccountController.java
│   │   │   ├── TransactionController.java
│   │   │   └── AdminController.java
│   │   ├── dto/
│   │   │   ├── ApiResponse.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── UserResponse.java
│   │   │   ├── AccountResponse.java
│   │   │   ├── DepositRequest.java
│   │   │   ├── WithdrawRequest.java
│   │   │   ├── TransferRequest.java
│   │   │   ├── TransactionResponse.java
│   │   │   ├── UpdateProfileRequest.java
│   │   │   ├── ChangePasswordRequest.java
│   │   │   └── AdminStatsResponse.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Account.java
│   │   │   ├── Transaction.java
│   │   │   ├── Role.java
│   │   │   ├── AccountType.java
│   │   │   ├── AccountStatus.java
│   │   │   ├── TransactionType.java
│   │   │   └── TransactionStatus.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── BankingException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── InsufficientBalanceException.java
│   │   │   └── AccountBlockedException.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── AccountRepository.java
│   │   │   └── TransactionRepository.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   └── CustomUserDetailsService.java
│   │   └── service/
│   │       ├── AuthService.java
│   │       ├── UserService.java
│   │       ├── AccountService.java
│   │       ├── TransactionService.java
│   │       └── AdminService.java
│   │
│   ├── src/main/resources/
│   │   ├── application.properties      # MySQL configuration
│   │   └── application-dev.properties  # H2 in-memory dev profile
│   └── pom.xml
│
├── frontend/
│   ├── index.html          # Landing page
│   ├── login.html          # Secure login & demo account filler
│   ├── register.html       # Customer registration & account generation
│   ├── dashboard.html      # Account balance card, actions, chart
│   ├── account.html        # Account details overview
│   ├── deposit.html        # Money deposit page
│   ├── withdraw.html       # Cash withdrawal page
│   ├── transfer.html       # Inter-account transfer & receipt modal
│   ├── transactions.html   # Searchable audit log & CSV export
│   ├── profile.html        # Profile update & password reset
│   ├── admin.html          # Admin management portal
│   ├── css/
│   │   └── style.css       # Custom banking UI stylesheet
│   └── js/
│       ├── auth.js         # JWT interceptor, auth guard, navbar
│       ├── dashboard.js    # Dashboard metrics & chart
│       ├── transactions.js # History filters & CSV export
│       ├── transfer.js     # Transfer logic & confirmation modal
│       └── admin.js        # Admin operations (block/unblock)
│
├── database.sql            # MySQL schema DDL & seed script
└── README.md
```

---

## Database Setup

### 1. MySQL Setup
1. Ensure MySQL Server is running on `localhost:3306`.
2. Open MySQL CLI or MySQL Workbench:
   ```sql
   CREATE DATABASE online_banking;
   ```
3. Run the schema creation script from the project root:
   ```bash
   mysql -u root -p online_banking < database.sql
   ```
4. Verify your credentials in `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/online_banking?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

---

## Backend Setup & Running

### Option A: Standard Run with MySQL
```powershell
cd backend
mvn spring-boot:run
```

### Option B: Immediate Run with In-Memory Dev Profile (No MySQL installation required)
```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
The server will start on `http://localhost:8080`.

### Pre-Seeded Demo Accounts
On startup, default demo accounts are automatically provisioned:
| Role | Name | Email | Password | Account Number | Initial Balance |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Administrator** | Bank Administrator | `admin@bank.com` | `Admin@123` | 1000000001 | ₹ 1,00,000.00 |
| **User 1** | Umesh Patil | `umesh@bank.com` | `User@123` | 1000123456 | ₹ 25,000.00 |
| **User 2** | John Doe | `john@bank.com` | `User@123` | 1000987654 | ₹ 15,000.00 |

---

## Frontend Setup & Running

The frontend consists of static HTML, CSS, and JavaScript. You can serve it using any local HTTP server or by opening directly in your browser:

### Option 1: Python Simple HTTP Server
```powershell
cd frontend
python -m http.server 3000
```
Open `http://localhost:3000/index.html` in your web browser.

### Option 2: VS Code Live Server / Node `serve`
```powershell
npx serve frontend -p 3000
```

### Option 3: Direct Browser Launch
Double click `frontend/index.html` to open in Google Chrome, Microsoft Edge, or Firefox.

---

## API Endpoints Reference

### Authentication Endpoints
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user and auto-create bank account | Public |
| `POST` | `/api/auth/login` | Authenticate user and issue JWT | Public |

### User Profile Endpoints
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | Fetch authenticated user's profile | User / Admin |
| `PUT` | `/api/users/me` | Update full name and mobile number | User / Admin |
| `PUT` | `/api/users/me/password` | Change user account password | User / Admin |

### Account Endpoints
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/accounts/me` | Fetch authenticated user's bank account | User / Admin |
| `GET` | `/api/accounts/{accountNumber}` | Preview beneficiary account holder details | User / Admin |

### Banking Transactions Endpoints
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/transactions/deposit` | Deposit funds into account | User / Admin |
| `POST` | `/api/transactions/withdraw` | Withdraw funds from account | User / Admin |
| `POST` | `/api/transactions/transfer` | Atomic transfer to another bank account | User / Admin |
| `GET` | `/api/transactions` | Search & filter user's transactions | User / Admin |
| `GET` | `/api/transactions/{id}` | Fetch individual transaction details | User / Admin |

### Admin Endpoints
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/users` | List and search all registered users | Admin Only |
| `GET` | `/api/admin/accounts` | List and search all bank accounts | Admin Only |
| `GET` | `/api/admin/transactions` | Audit all system transactions | Admin Only |
| `PUT` | `/api/admin/accounts/{id}/block` | Block bank account from operations | Admin Only |
| `PUT` | `/api/admin/accounts/{id}/unblock` | Unblock bank account | Admin Only |
| `GET` | `/api/admin/stats` | Retrieve platform-wide banking metrics | Admin Only |

---

## Authentication & Security

- **Stateless JWT**: Issued upon successful login with 24-hour expiration. Sent via the `Authorization: Bearer <TOKEN>` header.
- **BCrypt Encryption**: Passwords are never stored in plaintext and are never exposed in any API response.
- **Strict Ownership Validation**: Authenticated requests derive account identifiers directly from the verified JWT subject. A customer cannot access or debit another user's account by simply tampering with an ID.
- **Atomic Transfers**: Handled under `@Transactional(isolation = Isolation.REPEATABLE_READ)`. If either sender debit or receiver credit fails, all changes rollback instantly.
- **Account Block Protection**: Blocked accounts are rejected from all deposit, withdraw, and transfer endpoints.

---

## Testing with Postman

Import or configure the following test cases in Postman. Set the base URL to `http://localhost:8080`.

### 1. User Registration
- **Method**: `POST`
- **URL**: `{{base_url}}/api/auth/register`
- **Headers**: `Content-Type: application/json`
- **Body**:
  ```json
  {
    "fullName": "Alice Wonderland",
    "email": "alice@bank.com",
    "mobile": "9123456780",
    "accountType": "SAVINGS",
    "password": "Password@123",
    "confirmPassword": "Password@123"
  }
  ```
- **Expected Status**: `201 CREATED`

### 2. Duplicate Registration Conflict
- Repeat Step 1 with the same email.
- **Expected Status**: `409 CONFLICT`
- **Response**: `"Email is already registered."`

### 3. User Login
- **Method**: `POST`
- **URL**: `{{base_url}}/api/auth/login`
- **Body**:
  ```json
  {
    "email": "umesh@bank.com",
    "password": "User@123"
  }
  ```
- **Expected Status**: `200 OK`
- **Action**: Copy the returned `token` and set it as `Bearer <token>` in subsequent requests.

### 4. Invalid Login
- Submit an incorrect password.
- **Expected Status**: `401 UNAUTHORIZED`
- **Response**: `"Invalid email or password."`

### 5. Get My Account
- **Method**: `GET`
- **URL**: `{{base_url}}/api/accounts/me`
- **Headers**: `Authorization: Bearer <USER_TOKEN>`
- **Expected Status**: `200 OK`

### 6. Deposit Money
- **Method**: `POST`
- **URL**: `{{base_url}}/api/transactions/deposit`
- **Headers**: `Authorization: Bearer <USER_TOKEN>`
- **Body**:
  ```json
  {
    "amount": 2500.00,
    "description": "Quarterly dividend deposit"
  }
  ```
- **Expected Status**: `201 CREATED`

### 7. Withdraw Money
- **Method**: `POST`
- **URL**: `{{base_url}}/api/transactions/withdraw`
- **Headers**: `Authorization: Bearer <USER_TOKEN>`
- **Body**:
  ```json
  {
    "amount": 1000.00,
    "description": "Cash withdrawal"
  }
  ```
- **Expected Status**: `200 OK`

### 8. Insufficient Balance Check
- **Method**: `POST`
- **URL**: `{{base_url}}/api/transactions/withdraw`
- **Headers**: `Authorization: Bearer <USER_TOKEN>`
- **Body**:
  ```json
  {
    "amount": 99999999.00,
    "description": "Overdraft test"
  }
  ```
- **Expected Status**: `400 BAD REQUEST`
- **Response**: `"Insufficient balance for this transaction."`

### 9. Inter-Account Transfer
- **Method**: `POST`
- **URL**: `{{base_url}}/api/transactions/transfer`
- **Headers**: `Authorization: Bearer <USER_TOKEN>`
- **Body**:
  ```json
  {
    "receiverAccountNumber": "1000987654",
    "amount": 1500.00,
    "description": "Invoice #892 Payment"
  }
  ```
- **Expected Status**: `200 OK`

### 10. Invalid Receiver Transfer
- Transfer to a non-existent account number `9999999999`.
- **Expected Status**: `404 NOT FOUND`

### 11. Unauthorized API Access
- Request `GET /api/accounts/me` without the `Authorization` header.
- **Expected Status**: `401 UNAUTHORIZED`

### 12. Admin API Access Control
- Login with user token and attempt `GET /api/admin/users`.
- **Expected Status**: `403 FORBIDDEN`
- Login with admin token (`admin@bank.com`) and attempt `GET /api/admin/users`.
- **Expected Status**: `200 OK`

### 13. Blocked Account Operation Restriction
- As Admin, call `PUT /api/admin/accounts/2/block`.
- As User, attempt `POST /api/transactions/deposit`.
- **Expected Status**: `403 FORBIDDEN` (Account is blocked).

### 14. Transaction History with Filters
- **Method**: `GET`
- **URL**: `{{base_url}}/api/transactions?type=TRANSFER`
- **Headers**: `Authorization: Bearer <USER_TOKEN>`
- **Expected Status**: `200 OK`

---

## Screenshots

Below are key views of the application:
1. **Landing Page (`index.html`)**: Responsive financial hero with feature highlights and quick navigation.
2. **Dashboard (`dashboard.html`)**: Interactive balance card with account number masking, cash-flow doughnut chart, and recent transaction list.
3. **Transfer Interface (`transfer.html`)**: Dynamic beneficiary lookup, safety confirmation modal, and printable transaction receipt.
4. **Statement History (`transactions.html`)**: Multi-filter table with live keyword search and CSV statement export.
5. **Admin Portal (`admin.html`)**: Metric widgets, user auditing, and one-click account block/unblock toggles.

---

## Future Enhancements

- Email and SMS notification delivery on transactions
- Two-factor authentication (2FA) with OTP / Authenticator App
- PDF statement generation and download
- Loan application and EMI calculation management
- Fixed and recurring deposit lifecycle management
- Virtual debit/credit card generation and PIN management
- Advanced AI fraud detection mechanisms
- Containerization with Docker and Kubernetes deployment manifests

---

## Developer

**Umesh Patil**  
Online Banking Management System &bull; Full-Stack Simulation Project
