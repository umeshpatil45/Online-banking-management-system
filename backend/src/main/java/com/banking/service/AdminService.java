package com.banking.service;

import com.banking.dto.AccountResponse;
import com.banking.dto.AdminStatsResponse;
import com.banking.dto.TransactionResponse;
import com.banking.dto.UserResponse;
import com.banking.entity.*;
import com.banking.exception.ResourceNotFoundException;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public AdminService(UserRepository userRepository, AccountRepository accountRepository,
                        TransactionRepository transactionRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    public List<UserResponse> getAllUsers(String query) {
        List<User> users;
        if (query != null && !query.trim().isEmpty()) {
            users = userRepository.searchUsers(query.trim());
        } else {
            users = userRepository.findAll();
        }

        return users.stream().map(u -> {
            Account acc = accountRepository.findByUserId(u.getId()).orElse(null);
            return new UserResponse(
                    u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getMobile(),
                    u.getRole(),
                    acc != null ? acc.getAccountNumber() : "N/A",
                    u.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    public List<AccountResponse> getAllAccounts(String query) {
        List<Account> accounts;
        if (query != null && !query.trim().isEmpty()) {
            accounts = accountRepository.searchAccounts(query.trim());
        } else {
            accounts = accountRepository.findAll();
        }

        return accounts.stream().map(accountService::mapToResponse).collect(Collectors.toList());
    }

    public List<TransactionResponse> getAllTransactions(TransactionType type, String query) {
        List<Transaction> transactions = transactionRepository.filterAllTransactions(
                type,
                query != null && !query.trim().isEmpty() ? query.trim() : null
        );

        return transactions.stream().map(t -> new TransactionResponse(
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
        )).collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse blockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setStatus(AccountStatus.BLOCKED);
        Account updated = accountRepository.save(account);
        return accountService.mapToResponse(updated);
    }

    @Transactional
    public AccountResponse unblockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setStatus(AccountStatus.ACTIVE);
        Account updated = accountRepository.save(account);
        return accountService.mapToResponse(updated);
    }

    public AdminStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();

        List<Account> accounts = accountRepository.findAll();
        long activeCount = accounts.stream().filter(a -> a.getStatus() == AccountStatus.ACTIVE).count();
        long blockedCount = accounts.stream().filter(a -> a.getStatus() == AccountStatus.BLOCKED).count();

        BigDecimal depositVolume = transactionRepository.getTotalDepositVolume();
        if (depositVolume == null) depositVolume = BigDecimal.ZERO;

        BigDecimal transferVolume = transactionRepository.getTotalTransferVolume();
        if (transferVolume == null) transferVolume = BigDecimal.ZERO;

        return new AdminStatsResponse(
                totalUsers,
                totalAccounts,
                totalTransactions,
                depositVolume,
                transferVolume,
                activeCount,
                blockedCount
        );
    }
}
