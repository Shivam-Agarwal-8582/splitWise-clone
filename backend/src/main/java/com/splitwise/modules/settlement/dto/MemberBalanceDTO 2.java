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
public class MemberBalanceDTO {
    private UserDTO user;
    private BigDecimal totalPaid;
    private BigDecimal totalOwed;
    private BigDecimal netBalance; // Positive: to receive, Negative: owes
}

