package com.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class WithdrawRequest {

    @NotNull(message = "Withdrawal amount is required")
    @DecimalMin(value = "1.00", message = "Withdrawal amount must be at least 1.00")
    private BigDecimal amount;

    private String description;

    public WithdrawRequest() {
    }

    public WithdrawRequest(BigDecimal amount, String description) {
        this.amount = amount;
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
