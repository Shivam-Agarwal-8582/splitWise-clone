package com.splitwise.modules.expense.dto;

import com.splitwise.modules.expense.model.ExpensePayer;
import com.splitwise.modules.user.dto.UserDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpensePayerDTO {
    private Long id;

    @NotNull(message = "User ID is required for payer")
    private Long userId;

    private UserDTO user;

    @NotNull(message = "Paid amount is required")
    @DecimalMin(value = "0.01", message = "Paid amount must be at least 0.01")
    private BigDecimal amountPaid;

    public static ExpensePayerDTO fromEntity(ExpensePayer payer) {
        if (payer == null) return null;
        return ExpensePayerDTO.builder()
                .id(payer.getId())
                .userId(payer.getUser().getId())
                .user(UserDTO.fromEntity(payer.getUser()))
                .amountPaid(payer.getAmountPaid())
                .build();
    }
}

