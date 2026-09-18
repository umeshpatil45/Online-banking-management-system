package com.banking.controller;

import com.banking.dto.*;
import com.banking.entity.Role;
import com.banking.entity.TransactionType;
import com.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit processed successfully.", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WithdrawRequest request) {
        TransactionResponse response = transactionService.withdraw(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Withdrawal processed successfully.", response));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Transfer completed successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String query) {
        List<TransactionResponse> list = transactionService.getUserTransactions(
                userDetails.getUsername(), type, startDate, endDate, query
        );
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully.", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()));
        TransactionResponse response = transactionService.getTransactionById(id, userDetails.getUsername(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully.", response));
    }
}
