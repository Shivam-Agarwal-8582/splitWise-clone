package com.splitwise.modules.expense.dto;

import com.splitwise.modules.expense.model.Expense;
import com.splitwise.modules.expense.model.ExpenseCategory;
import com.splitwise.modules.expense.model.SplitType;
import com.splitwise.modules.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDTO {
    private Long id;
    private Long groupId;
    private String description;
    private ExpenseCategory category;
    private BigDecimal totalAmount;
    private String currency;
    private SplitType splitType;
    private LocalDate expenseDate;
    private UserDTO createdBy;
    private List<ExpensePayerDTO> payers;
    private List<ExpenseSplitDTO> splits;
    private Instant createdAt;
    private Instant updatedAt;

    public static ExpenseResponseDTO fromEntity(Expense expense) {
        if (expense == null) return null;
        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .description(expense.getDescription())
                .category(expense.getCategory())
                .totalAmount(expense.getTotalAmount())
                .currency(expense.getCurrency())
                .splitType(expense.getSplitType())
                .expenseDate(expense.getExpenseDate())
                .createdBy(UserDTO.fromEntity(expense.getCreatedBy()))
                .payers(expense.getPayers() != null ?
                        expense.getPayers().stream().map(ExpensePayerDTO::fromEntity).collect(Collectors.toList())
                        : List.of())
                .splits(expense.getSplits() != null ?
                        expense.getSplits().stream().map(ExpenseSplitDTO::fromEntity).collect(Collectors.toList())
                        : List.of())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}

