package com.banking.dto;

import java.math.BigDecimal;

public class AdminStatsResponse {

    private long totalUsers;
    private long totalAccounts;
    private long totalTransactions;
    private BigDecimal totalDepositVolume;
    private BigDecimal totalTransferVolume;
    private long activeAccountsCount;
    private long blockedAccountsCount;

    public AdminStatsResponse() {
    }

    public AdminStatsResponse(long totalUsers, long totalAccounts, long totalTransactions,
                              BigDecimal totalDepositVolume, BigDecimal totalTransferVolume,
                              long activeAccountsCount, long blockedAccountsCount) {
        this.totalUsers = totalUsers;
        this.totalAccounts = totalAccounts;
        this.totalTransactions = totalTransactions;
        this.totalDepositVolume = totalDepositVolume;
        this.totalTransferVolume = totalTransferVolume;
        this.activeAccountsCount = activeAccountsCount;
        this.blockedAccountsCount = blockedAccountsCount;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(long totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getTotalDepositVolume() {
        return totalDepositVolume;
    }

    public void setTotalDepositVolume(BigDecimal totalDepositVolume) {
        this.totalDepositVolume = totalDepositVolume;
    }

    public BigDecimal getTotalTransferVolume() {
        return totalTransferVolume;
    }

    public void setTotalTransferVolume(BigDecimal totalTransferVolume) {
        this.totalTransferVolume = totalTransferVolume;
    }

    public long getActiveAccountsCount() {
        return activeAccountsCount;
    }

    public void setActiveAccountsCount(long activeAccountsCount) {
        this.activeAccountsCount = activeAccountsCount;
    }

    public long getBlockedAccountsCount() {
        return blockedAccountsCount;
    }

    public void setBlockedAccountsCount(long blockedAccountsCount) {
        this.blockedAccountsCount = blockedAccountsCount;
    }
}
