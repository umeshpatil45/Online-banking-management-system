package com.banking.service;

import com.banking.dto.DepositRequest;
import com.banking.dto.TransactionResponse;
import com.banking.dto.TransferRequest;
import com.banking.dto.WithdrawRequest;
import com.banking.entity.*;
import com.banking.exception.AccountBlockedException;
import com.banking.exception.BankingException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.exception.ResourceNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransactionResponse deposit(String userEmail, DepositRequest request) {
        Account account = accountRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "user email", userEmail));

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException(account.getAccountNumber());
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Deposit amount must be greater than zero.", HttpStatus.BAD_REQUEST);
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        String reference = generateReference();
        String desc = request.getDescription() != null && !request.getDescription().trim().isEmpty()
                ? request.getDescription().trim()
                : "Deposit into Account " + account.getAccountNumber();

        Transaction transaction = new Transaction(
                reference,
                "SELF",
                account.getAccountNumber(),
                TransactionType.DEPOSIT,
                amount,
                newBalance,
                desc,
                TransactionStatus.SUCCESS
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransactionResponse withdraw(String userEmail, WithdrawRequest request) {
        Account account = accountRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "user email", userEmail));

        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException(account.getAccountNumber());
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Withdrawal amount must be greater than zero.", HttpStatus.BAD_REQUEST);
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transaction.");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        String reference = generateReference();
        String desc = request.getDescription() != null && !request.getDescription().trim().isEmpty()
                ? request.getDescription().trim()
                : "Withdrawal from Account " + account.getAccountNumber();

        Transaction transaction = new Transaction(
                reference,
                account.getAccountNumber(),
                "SELF",
                TransactionType.WITHDRAW,
                amount,
                newBalance,
                desc,
                TransactionStatus.SUCCESS
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransactionResponse transfer(String userEmail, TransferRequest request) {
        Account senderAccount = accountRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Sender Account", "user email", userEmail));

        if (senderAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException(senderAccount.getAccountNumber());
        }

        String receiverAccNumber = request.getReceiverAccountNumber().trim();
        Account receiverAccount = accountRepository.findByAccountNumber(receiverAccNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found: " + receiverAccNumber));

        if (senderAccount.getAccountNumber().equals(receiverAccount.getAccountNumber())) {
            throw new BankingException("Sender and receiver accounts cannot be the same.", HttpStatus.BAD_REQUEST);
        }

        if (receiverAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new BankingException("Transfer failed: Receiver account is blocked.", HttpStatus.BAD_REQUEST);
        }

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankingException("Transfer amount must be greater than zero.", HttpStatus.BAD_REQUEST);
        }

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transaction.");
        }

        // 1. Deduct amount from sender
        BigDecimal senderNewBalance = senderAccount.getBalance().subtract(amount);
        senderAccount.setBalance(senderNewBalance);
        accountRepository.save(senderAccount);

        // 2. Add amount to receiver
        BigDecimal receiverNewBalance = receiverAccount.getBalance().add(amount);
        receiverAccount.setBalance(receiverNewBalance);
        accountRepository.save(receiverAccount);

        // 3. Create single atomic transaction record
        String reference = generateReference();
        String desc = request.getDescription() != null && !request.getDescription().trim().isEmpty()
                ? request.getDescription().trim()
                : String.format("Fund Transfer from %s to %s", senderAccount.getAccountNumber(), receiverAccount.getAccountNumber());

        Transaction transaction = new Transaction(
                reference,
                senderAccount.getAccountNumber(),
                receiverAccount.getAccountNumber(),
                TransactionType.TRANSFER,
                amount,
                senderNewBalance,
                desc,
                TransactionStatus.SUCCESS
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    public List<TransactionResponse> getUserTransactions(String userEmail, TransactionType type,
                                                        LocalDate startDate, LocalDate endDate, String query) {
        Account account = accountRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "user email", userEmail));

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        List<Transaction> list = transactionRepository.filterUserTransactions(
                account.getAccountNumber(),
                type,
                startDateTime,
                endDateTime,
                query != null ? query.trim() : null
        );

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long id, String userEmail, boolean isAdmin) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        if (!isAdmin) {
            Account account = accountRepository.findByUserEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Account", "user email", userEmail));

            String accNum = account.getAccountNumber();
            if (!accNum.equals(transaction.getSenderAccount()) && !accNum.equals(transaction.getReceiverAccount())) {
                throw new BankingException("Access denied: You do not have permission to view this transaction.", HttpStatus.FORBIDDEN);
            }
        }

        return mapToResponse(transaction);
    }

    private String generateReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randPart = 1000 + RANDOM.nextInt(9000);
        return "TXN" + datePart + randPart;
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getTransactionReference(),
                t.getSenderAccount(),
                t.getReceiverAccount(),
                t.getTransactionType(),
                t.getAmount(),
                t.getBalanceAfterTransaction(),
                t.getDescription(),
                t.getTransactionStatus(),
                t.getCreatedAt()
        );
    }
}
