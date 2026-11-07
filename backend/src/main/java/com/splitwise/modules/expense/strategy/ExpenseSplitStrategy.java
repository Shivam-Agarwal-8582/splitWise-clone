package com.splitwise.modules.expense.strategy;

import com.splitwise.modules.expense.model.SplitType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ExpenseSplitStrategy {
    SplitType getSplitType();

    /**
     * Calculates the owed amount for each participant.
     * Guaranteed: sum(values in returned map) == totalAmount (scaled to 2 decimal places)
     */
    Map<Long, BigDecimal> calculateSplits(BigDecimal totalAmount, List<SplitParticipantItem> participants);
}

