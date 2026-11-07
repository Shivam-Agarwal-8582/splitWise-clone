package com.splitwise.modules.expense.strategy;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.common.utils.MoneyUtils;
import com.splitwise.modules.expense.model.SplitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExactSplitStrategy implements ExpenseSplitStrategy {

    @Override
    public SplitType getSplitType() {
        return SplitType.EXACT;
    }

    @Override
    public Map<Long, BigDecimal> calculateSplits(BigDecimal totalAmount, List<SplitParticipantItem> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new BadRequestException("Participants list cannot be empty for exact split");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total amount must be greater than zero");
        }

        BigDecimal scaledTotal = MoneyUtils.round(totalAmount);
        BigDecimal runningSum = BigDecimal.ZERO;
        Map<Long, BigDecimal> result = new LinkedHashMap<>();

        for (SplitParticipantItem p : participants) {
            if (p.getSplitValue() == null || p.getSplitValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Exact split amount cannot be negative or null for user " + p.getUserId());
            }
            BigDecimal amount = MoneyUtils.round(p.getSplitValue());
            runningSum = runningSum.add(amount);
            result.put(p.getUserId(), amount);
        }

        if (runningSum.compareTo(scaledTotal) != 0) {
            throw new BadRequestException(String.format(
                    "Exact split amounts sum to %s, but total expense is %s. Difference: %s",
                    runningSum, scaledTotal, scaledTotal.subtract(runningSum)
            ));
        }

        return result;
    }
}

