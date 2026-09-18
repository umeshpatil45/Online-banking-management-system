package com.banking.service;

import com.banking.dto.AuthResponse;
import com.banking.dto.LoginRequest;
import com.banking.dto.RegisterRequest;
import com.banking.entity.*;
import com.banking.exception.BankingException;
import com.banking.exception.ResourceNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import com.banking.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${banking.account.initial-balance:5000.00}")
    private BigDecimal initialBalance;

    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthService(UserRepository userRepository, AccountRepository accountRepository,
                       TransactionRepository transactionRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BankingException("Passwords do not match.", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BankingException("Email is already registered.", HttpStatus.CONFLICT);
        }

        if (userRepository.existsByMobile(request.getMobile().trim())) {
            throw new BankingException("Mobile number is already registered.", HttpStatus.CONFLICT);
        }

        // 1. Create User
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setMobile(request.getMobile().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        User savedUser = userRepository.save(user);

        // 2. Generate unique 10-digit Account Number
        String accountNumber = generateUniqueAccountNumber();

        // 3. Create Account
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setUser(savedUser);
        account.setAccountType(request.getAccountType() != null ? request.getAccountType() : AccountType.SAVINGS);
        account.setBalance(initialBalance);
        account.setStatus(AccountStatus.ACTIVE);
        Account savedAccount = accountRepository.save(account);

        // 4. Record Initial Welcome Deposit Transaction
        String txnRef = generateTransactionReference();
        Transaction welcomeTxn = new Transaction(
                txnRef,
                "SYSTEM_INIT",
                accountNumber,
                TransactionType.DEPOSIT,
                initialBalance,
                initialBalance,
                "Initial Welcome Deposit upon Account Registration",
                TransactionStatus.SUCCESS
        );
        transactionRepository.save(welcomeTxn);

        // 5. Generate JWT Token
        String token = tokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole(), savedUser.getId());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedAccount.getAccountNumber()
        );
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
        String accountNumber = account != null ? account.getAccountNumber() : "N/A";

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole(), user.getId());

        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                accountNumber
        );
    }

    private String generateUniqueAccountNumber() {
        String accNumber;
        do {
            // Generate 10-digit account number starting with 1000
            int suffix = 100000 + RANDOM.nextInt(900000);
            accNumber = "1000" + suffix;
        } while (accountRepository.existsByAccountNumber(accNumber));
        return accNumber;
    }

    private String generateTransactionReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randPart = 1000 + RANDOM.nextInt(9000);
        return "TXN" + datePart + randPart;
    }
}
