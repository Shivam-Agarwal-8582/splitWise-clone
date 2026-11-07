package com.splitwise.modules.expense.strategy;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.common.utils.MoneyUtils;
import com.splitwise.modules.expense.model.SplitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PercentageSplitStrategy implements ExpenseSplitStrategy {

    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal PERCENT_TOLERANCE = new BigDecimal("0.01");

    @Override
    public SplitType getSplitType() {
        return SplitType.PERCENTAGE;
    }

    @Override
    public Map<Long, BigDecimal> calculateSplits(BigDecimal totalAmount, List<SplitParticipantItem> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new BadRequestException("Participants list cannot be empty for percentage split");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total amount must be greater than zero");
        }

        BigDecimal scaledTotal = MoneyUtils.round(totalAmount);
        BigDecimal totalPercentage = BigDecimal.ZERO;

        for (SplitParticipantItem p : participants) {
            if (p.getSplitValue() == null || p.getSplitValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Percentage cannot be negative or null for user " + p.getUserId());
            }
            totalPercentage = totalPercentage.add(p.getSplitValue());
        }

        if (totalPercentage.subtract(HUNDRED).abs().compareTo(PERCENT_TOLERANCE) > 0) {
            throw new BadRequestException(String.format(
                    "Total percentage must sum to 100%%, but currently sums to %s%%", totalPercentage
            ));
        }

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal totalAllocated = BigDecimal.ZERO;
        Long maxPercentageUserId = null;
        BigDecimal maxPercentage = BigDecimal.valueOf(-1);

        for (SplitParticipantItem p : participants) {
            BigDecimal owed = scaledTotal.multiply(p.getSplitValue())
                    .divide(HUNDRED, 2, RoundingMode.HALF_EVEN);
            result.put(p.getUserId(), owed);
            totalAllocated = totalAllocated.add(owed);

            if (p.getSplitValue().compareTo(maxPercentage) > 0) {
                maxPercentage = p.getSplitValue();
                maxPercentageUserId = p.getUserId();
            }
        }

        // Penny reconciliation
        BigDecimal discrepancy = scaledTotal.subtract(totalAllocated);
        if (discrepancy.compareTo(BigDecimal.ZERO) != 0 && maxPercentageUserId != null) {
            BigDecimal adjusted = result.get(maxPercentageUserId).add(discrepancy);
            result.put(maxPercentageUserId, adjusted);
        }

        return result;
    }
}

