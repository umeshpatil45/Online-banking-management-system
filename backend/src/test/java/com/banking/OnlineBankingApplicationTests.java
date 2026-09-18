package com.banking;

import com.banking.dto.*;
import com.banking.entity.AccountStatus;
import com.banking.entity.Role;
import com.banking.exception.AccountBlockedException;
import com.banking.exception.BankingException;
import com.banking.exception.InsufficientBalanceException;
import com.banking.service.AdminService;
import com.banking.service.AuthService;
import com.banking.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class OnlineBankingApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AdminService adminService;

    @Test
    void contextLoads() {
        assertNotNull(authService);
        assertNotNull(transactionService);
        assertNotNull(adminService);
    }

    @Test
    void testUserRegistrationAndDuplicateConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Alice Wonderland");
        request.setEmail("alice@bank.com");
        request.setMobile("9123456780");
        request.setPassword("Password@123");
        request.setConfirmPassword("Password@123");

        AuthResponse response = authService.register(request);
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("alice@bank.com", response.getEmail());
        assertEquals(Role.ROLE_USER, response.getRole());
        assertNotNull(response.getAccountNumber());

        // Attempt duplicate registration with same email
        assertThrows(BankingException.class, () -> authService.register(request));
    }

    @Test
    void testLoginSuccessAndInvalidCredentials() {
        LoginRequest login = new LoginRequest("umesh@bank.com", "User@123");
        AuthResponse response = authService.login(login);
        assertNotNull(response.getToken());
        assertEquals("umesh@bank.com", response.getEmail());

        // Bad password
        LoginRequest badLogin = new LoginRequest("umesh@bank.com", "WrongPassword");
        assertThrows(Exception.class, () -> authService.login(badLogin));
    }

    @Test
    void testDepositAndWithdraw() {
        // Initial balance for umesh is 25,000.00
        DepositRequest dep = new DepositRequest(new BigDecimal("1500.00"), "Test Deposit");
        TransactionResponse depRes = transactionService.deposit("umesh@bank.com", dep);
        assertNotNull(depRes);
        assertEquals(new BigDecimal("1500.00"), depRes.getAmount());

        WithdrawRequest wit = new WithdrawRequest(new BigDecimal("500.00"), "Test Withdraw");
        TransactionResponse witRes = transactionService.withdraw("umesh@bank.com", wit);
        assertNotNull(witRes);
        assertEquals(new BigDecimal("500.00"), witRes.getAmount());
    }

    @Test
    void testWithdrawInsufficientBalance() {
        WithdrawRequest wit = new WithdrawRequest(new BigDecimal("99999999.00"), "Too Much");
        assertThrows(InsufficientBalanceException.class, () -> transactionService.withdraw("umesh@bank.com", wit));
    }

    @Test
    void testTransferBetweenAccounts() {
        // umesh (1000123456) transfers to john (1000987654)
        TransferRequest req = new TransferRequest("1000987654", new BigDecimal("1000.00"), "Rent share");
        TransactionResponse res = transactionService.transfer("umesh@bank.com", req);

        assertNotNull(res);
        assertNotNull(res.getTransactionReference());
        assertEquals("1000123456", res.getSenderAccount());
        assertEquals("1000987654", res.getReceiverAccount());
        assertEquals(new BigDecimal("1000.00"), res.getAmount());
    }

    @Test
    void testTransferToSelfFails() {
        TransferRequest req = new TransferRequest("1000123456", new BigDecimal("100.00"), "Self transfer");
        assertThrows(BankingException.class, () -> transactionService.transfer("umesh@bank.com", req));
    }

    @Test
    void testAccountBlockAndUnblock() {
        // Find john's account (id = 3 or query by number)
        List<AccountResponse> accounts = adminService.getAllAccounts("1000987654");
        assertFalse(accounts.isEmpty());
        Long johnAccId = accounts.get(0).getId();

        // Block John's account
        AccountResponse blocked = adminService.blockAccount(johnAccId);
        assertEquals(AccountStatus.BLOCKED, blocked.getStatus());

        // Attempt transfer to blocked receiver
        TransferRequest req = new TransferRequest("1000987654", new BigDecimal("50.00"), "Try transfer to blocked");
        assertThrows(BankingException.class, () -> transactionService.transfer("umesh@bank.com", req));

        // Attempt deposit on blocked account
        DepositRequest dep = new DepositRequest(new BigDecimal("100.00"), "Blocked deposit");
        assertThrows(AccountBlockedException.class, () -> transactionService.deposit("john@bank.com", dep));

        // Unblock John's account
        AccountResponse unblocked = adminService.unblockAccount(johnAccId);
        assertEquals(AccountStatus.ACTIVE, unblocked.getStatus());
    }

    @Test
    void testAdminStats() {
        AdminStatsResponse stats = adminService.getDashboardStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalUsers() >= 3);
        assertTrue(stats.getTotalAccounts() >= 3);
        assertTrue(stats.getTotalTransactions() >= 3);
    }
}
