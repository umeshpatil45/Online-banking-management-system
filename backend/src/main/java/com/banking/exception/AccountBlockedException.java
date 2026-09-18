package com.banking.exception;

import org.springframework.http.HttpStatus;

public class AccountBlockedException extends BankingException {

    public AccountBlockedException(String accountNumber) {
        super(String.format("Account %s is BLOCKED. Banking operations are restricted. Please contact support.", accountNumber), HttpStatus.FORBIDDEN);
    }
}
