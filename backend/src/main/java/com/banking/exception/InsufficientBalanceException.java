package com.banking.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BankingException {

    public InsufficientBalanceException() {
        super("Insufficient balance for this transaction", HttpStatus.BAD_REQUEST);
    }

    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
