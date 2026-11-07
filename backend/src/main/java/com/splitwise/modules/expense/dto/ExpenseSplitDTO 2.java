package com.splitwise.modules.expense.dto;

import com.splitwise.modules.expense.model.ExpenseSplit;
import com.splitwise.modules.user.dto.UserDTO;
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
public class ExpenseSplitDTO {
    private Long id;

    @NotNull(message = "User ID is required for split participant")
    private Long userId;

    private UserDTO user;
    private BigDecimal owedAmount;
    private BigDecimal splitValue; // percentage, exact amount, or shares

    public static ExpenseSplitDTO fromEntity(ExpenseSplit split) {
        if (split == null) return null;
        return ExpenseSplitDTO.builder()
                .id(split.getId())
                .userId(split.getUser().getId())
                .user(UserDTO.fromEntity(split.getUser()))
                .owedAmount(split.getOwedAmount())
                .splitValue(split.getSplitValue())
                .build();
    }
}

