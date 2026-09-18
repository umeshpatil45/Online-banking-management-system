package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.dto.ApiResponse;
import com.banking.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        AccountResponse response = accountService.getMyAccount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Account details retrieved successfully.", response));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account found.", response));
    }
}
