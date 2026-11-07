package com.splitwise.modules.settlement.dto;

import com.splitwise.modules.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedDebtDTO {
    private UserDTO debtor;   // person who owes money
    private UserDTO creditor; // person who receives money
    private BigDecimal amount;
    private String currency;
}

