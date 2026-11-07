package com.splitwise.modules.expense.strategy;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.modules.expense.model.SplitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EqualSplitStrategy implements ExpenseSplitStrategy {

    private static final BigDecimal ONE_CENT = new BigDecimal("0.01");

    @Override
    public SplitType getSplitType() {
        return SplitType.EQUAL;
    }

    @Override
    public Map<Long, BigDecimal> calculateSplits(BigDecimal totalAmount, List<SplitParticipantItem> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new BadRequestException("Participants list cannot be empty for equal split");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total amount must be greater than zero");
        }

        int count = participants.size();
        BigDecimal countBd = BigDecimal.valueOf(count);

        // Floor division to nearest cent
        BigDecimal baseShare = totalAmount.divide(countBd, 2, RoundingMode.FLOOR);
        BigDecimal accountedFor = baseShare.multiply(countBd);
        BigDecimal remainder = totalAmount.subtract(accountedFor);

        // Number of pennies to distribute
        int centsToDistribute = remainder.movePointRight(2).intValueExact();

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            Long userId = participants.get(i).getUserId();
            BigDecimal owed = baseShare;
            if (i < centsToDistribute) {
                owed = owed.add(ONE_CENT);
            }
            result.put(userId, owed);
        }

        return result;
    }
}

