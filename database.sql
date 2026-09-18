 -- ==========================================================
-- Online Banking Management System - Database Setup Script
-- Database: online_banking
-- Developer: Umesh Patil
-- ==========================================================

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS online_banking 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE online_banking;

-- 2. Drop existing tables if re-initializing
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 3. Create USERS Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    mobile VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Create ACCOUNTS Table
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL DEFAULT 'SAVINGS',
    balance DECIMAL(15, 2) NOT NULL DEFAULT 5000.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_account_number (account_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Create TRANSACTIONS Table
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(40) NOT NULL UNIQUE,
    sender_account VARCHAR(20) DEFAULT NULL,
    receiver_account VARCHAR(20) DEFAULT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    balance_after_transaction DECIMAL(15, 2) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    transaction_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_txn_reference (transaction_reference),
    INDEX idx_txn_sender (sender_account),
    INDEX idx_txn_receiver (receiver_account),
    INDEX idx_txn_type (transaction_type),
    INDEX idx_txn_date (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================================
-- Initial Demo Seed Data
-- Passwords below are hashed using BCrypt:
-- Admin@123 -> $2a$10$vP.C...
-- User@123  -> $2a$10$eO7k...
-- Note: Spring Boot DataInitializer also auto-seeds these
-- if the database is initially empty!
-- ==========================================================

-- Seed Admin (admin@bank.com / Admin@123)
INSERT INTO users (id, full_name, email, mobile, password, role, created_at, updated_at) 
VALUES (1, 'Bank Administrator', 'admin@bank.com', '9876543210', '$2a$10$R9n5r6u3B2jYw402qPjP/eh8/2v7W0jA2z2f5E4D3c2B1a0Z9y8x.', 'ROLE_ADMIN', NOW(), NOW());

INSERT INTO accounts (id, account_number, user_id, account_type, balance, status, created_at)
VALUES (1, '1000000001', 1, 'CURRENT', 100000.00, 'ACTIVE', NOW());

-- Seed Demo User 1: Umesh Patil (umesh@bank.com / User@123)
INSERT INTO users (id, full_name, email, mobile, password, role, created_at, updated_at) 
VALUES (2, 'Umesh Patil', 'umesh@bank.com', '9876543211', '$2a$10$1Y5i/oG0J6eZ5fT3r.1eAeG1a8q6V6r0t8z7x6y5w4v3u2t1s0r9.', 'ROLE_USER', NOW(), NOW());

INSERT INTO accounts (id, account_number, user_id, account_type, balance, status, created_at)
VALUES (2, '1000123456', 2, 'SAVINGS', 25000.00, 'ACTIVE', NOW());

-- Seed Demo User 2: John Doe (john@bank.com / User@123)
INSERT INTO users (id, full_name, email, mobile, password, role, created_at, updated_at) 
VALUES (3, 'John Doe', 'john@bank.com', '9876543212', '$2a$10$1Y5i/oG0J6eZ5fT3r.1eAeG1a8q6V6r0t8z7x6y5w4v3u2t1s0r9.', 'ROLE_USER', NOW(), NOW());

INSERT INTO accounts (id, account_number, user_id, account_type, balance, status, created_at)
VALUES (3, '1000987654', 3, 'CURRENT', 15000.00, 'ACTIVE', NOW());

-- Seed Sample Transactions
INSERT INTO transactions (transaction_reference, sender_account, receiver_account, transaction_type, amount, balance_after_transaction, description, transaction_status, created_at)
VALUES 
('TXN20260901001', 'CASH_DEPOSIT', '1000123456', 'DEPOSIT', 20000.00, 20000.00, 'Salary deposit for August', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('TXN20260905002', '1000123456', 'ATM_WITHDRAWAL', 'WITHDRAW', 3000.00, 17000.00, 'ATM cash withdrawal', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('TXN20260908003', '1000987654', '1000123456', 'TRANSFER', 8000.00, 25000.00, 'Consulting fees transfer', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 2 DAY));
