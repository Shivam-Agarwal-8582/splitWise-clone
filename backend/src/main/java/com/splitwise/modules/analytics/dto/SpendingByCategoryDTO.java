package com.splitwise.modules.analytics.dto;

import com.splitwise.modules.expense.model.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingByCategoryDTO {
    private ExpenseCategory category;
    private BigDecimal amount;
    private double percentage;
}

