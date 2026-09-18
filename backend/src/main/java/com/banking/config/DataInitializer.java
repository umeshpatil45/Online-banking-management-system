package com.banking.config;

import com.banking.entity.*;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, AccountRepository accountRepository,
                           TransactionRepository transactionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding initial demo data for Online Banking Management System...");

            // 1. Admin User
            User admin = new User(
                    "Bank Administrator",
                    "admin@bank.com",
                    "9876543210",
                    passwordEncoder.encode("Admin@123"),
                    Role.ROLE_ADMIN
            );
            admin = userRepository.save(admin);

            Account adminAccount = new Account(
                    "1000000001",
                    admin,
                    AccountType.CURRENT,
                    new BigDecimal("100000.00"),
                    AccountStatus.ACTIVE
            );
            accountRepository.save(adminAccount);

            // 2. Demo User 1: Umesh Patil
            User user1 = new User(
                    "Umesh Patil",
                    "umesh@bank.com",
                    "9876543211",
                    passwordEncoder.encode("User@123"),
                    Role.ROLE_USER
            );
            user1 = userRepository.save(user1);

            Account account1 = new Account(
                    "1000123456",
                    user1,
                    AccountType.SAVINGS,
                    new BigDecimal("25000.00"),
                    AccountStatus.ACTIVE
            );
            account1 = accountRepository.save(account1);

            // 3. Demo User 2: John Doe
            User user2 = new User(
                    "John Doe",
                    "john@bank.com",
                    "9876543212",
                    passwordEncoder.encode("User@123"),
                    Role.ROLE_USER
            );
            user2 = userRepository.save(user2);

            Account account2 = new Account(
                    "1000987654",
                    user2,
                    AccountType.CURRENT,
                    new BigDecimal("15000.00"),
                    AccountStatus.ACTIVE
            );
            account2 = accountRepository.save(account2);

            // 4. Sample Transactions
            Transaction t1 = new Transaction(
                    "TXN20260901001",
                    "CASH_DEPOSIT",
                    "1000123456",
                    TransactionType.DEPOSIT,
                    new BigDecimal("20000.00"),
                    new BigDecimal("20000.00"),
                    "Salary deposit for August",
                    TransactionStatus.SUCCESS
            );
            t1.setCreatedAt(LocalDateTime.now().minusDays(10));
            transactionRepository.save(t1);

            Transaction t2 = new Transaction(
                    "TXN20260905002",
                    "1000123456",
                    "ATM_WITHDRAWAL",
                    TransactionType.WITHDRAW,
                    new BigDecimal("3000.00"),
                    new BigDecimal("17000.00"),
                    "ATM cash withdrawal",
                    TransactionStatus.SUCCESS
            );
            t2.setCreatedAt(LocalDateTime.now().minusDays(5));
            transactionRepository.save(t2);

            Transaction t3 = new Transaction(
                    "TXN20260908003",
                    "1000987654",
                    "1000123456",
                    TransactionType.TRANSFER,
                    new BigDecimal("8000.00"),
                    new BigDecimal("25000.00"),
                    "Consulting fees transfer",
                    TransactionStatus.SUCCESS
            );
            t3.setCreatedAt(LocalDateTime.now().minusDays(2));
            transactionRepository.save(t3);

            log.info("Default seed data initialized successfully.");
            log.info("Admin: admin@bank.com / Admin@123");
            log.info("User: umesh@bank.com / User@123 (Account: 1000123456, Balance: ₹25,000.00)");
            log.info("User: john@bank.com / User@123 (Account: 1000987654, Balance: ₹15,000.00)");
        }
    }
}
