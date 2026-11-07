package com.splitwise.modules.settlement.dto;

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
public class GroupBalanceSummaryDTO {
    private Long groupId;
    private String currency;
    private BigDecimal totalGroupSpending;
    private List<MemberBalanceDTO> memberBalances;
    private List<SimplifiedDebtDTO> simplifiedDebts;
}

