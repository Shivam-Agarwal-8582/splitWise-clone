package com.splitwise.modules.expense.dto;

import com.splitwise.modules.expense.model.ExpenseCategory;
import com.splitwise.modules.expense.model.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateExpenseRequest {

    @NotBlank(message = "Expense description is required")
    private String description;

    private ExpenseCategory category = ExpenseCategory.OTHER;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    private String currency = "USD";

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    private LocalDate expenseDate = LocalDate.now();

    @NotEmpty(message = "At least one payer is required")
    @Valid
    private List<ExpensePayerDTO> payers;

    @NotEmpty(message = "At least one split participant is required")
    @Valid
    private List<ExpenseSplitDTO> splits;
}

