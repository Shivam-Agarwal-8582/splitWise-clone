package com.splitwise.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupAnalyticsDTO {
    private Long groupId;
    private BigDecimal totalSpending;
    private List<SpendingByCategoryDTO> categoryBreakdown;
    private List<MonthlySpendingDTO> monthlyTrends;
}

