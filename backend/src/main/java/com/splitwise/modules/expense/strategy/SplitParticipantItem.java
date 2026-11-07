package com.splitwise.modules.expense.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitParticipantItem {
    private Long userId;
    private BigDecimal splitValue; // amount, percentage, or shares
}

