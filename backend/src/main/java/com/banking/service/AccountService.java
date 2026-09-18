package com.banking.service;

import com.banking.dto.AccountResponse;
import com.banking.entity.Account;
import com.banking.exception.ResourceNotFoundException;
import com.banking.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse getMyAccount(String userEmail) {
        Account account = accountRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Bank Account", "user email", userEmail));

        return mapToResponse(account);
    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "account number", accountNumber));

        return mapToResponse(account);
    }

    public AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getUser() != null ? account.getUser().getId() : null,
                account.getUser() != null ? account.getUser().getFullName() : "N/A",
                account.getUser() != null ? account.getUser().getEmail() : "N/A",
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }
}
