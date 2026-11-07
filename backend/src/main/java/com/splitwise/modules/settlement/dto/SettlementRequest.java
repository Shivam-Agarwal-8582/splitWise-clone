package com.splitwise.modules.settlement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SettlementRequest {

    @NotNull(message = "Payer user ID is required")
    private Long payerId;

    @NotNull(message = "Payee user ID is required")
    private Long payeeId;

    @NotNull(message = "Settlement amount is required")
    @DecimalMin(value = "0.01", message = "Settlement amount must be greater than zero")
    private BigDecimal amount;

    private String currency = "USD";
    private String notes;
}

