package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.dto.AdminStatsResponse;
import com.banking.dto.ApiResponse;
import com.banking.dto.TransactionResponse;
import com.banking.dto.UserResponse;
import com.banking.entity.TransactionType;
import com.banking.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String query) {
        List<UserResponse> users = adminService.getAllUsers(query);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully.", users));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(
            @RequestParam(required = false) String query) {
        List<AccountResponse> accounts = adminService.getAllAccounts(query);
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully.", accounts));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String query) {
        List<TransactionResponse> transactions = adminService.getAllTransactions(type, query);
        return ResponseEntity.ok(ApiResponse.success("All transactions retrieved successfully.", transactions));
    }

    @PutMapping("/accounts/{id}/block")
    public ResponseEntity<ApiResponse<AccountResponse>> blockAccount(@PathVariable Long id) {
        AccountResponse response = adminService.blockAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Account blocked successfully.", response));
    }

    @PutMapping("/accounts/{id}/unblock")
    public ResponseEntity<ApiResponse<AccountResponse>> unblockAccount(@PathVariable Long id) {
        AccountResponse response = adminService.unblockAccount(id);
        return ResponseEntity.ok(ApiResponse.success("Account unblocked successfully.", response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        AdminStatsResponse response = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved successfully.", response));
    }
}
