package com.splitwise.modules.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardSummaryDTO {
    private BigDecimal totalOwed;
    private BigDecimal totalOwe;
    private BigDecimal netBalance;
    private int activeGroupCount;
    private String currency;
}

