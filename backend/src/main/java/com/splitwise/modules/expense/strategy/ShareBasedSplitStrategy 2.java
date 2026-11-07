package com.splitwise.modules.expense.strategy;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.common.utils.MoneyUtils;
import com.splitwise.modules.expense.model.SplitType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class ShareBasedSplitStrategy implements ExpenseSplitStrategy {

    private static final BigDecimal ONE_CENT = new BigDecimal("0.01");

    @Override
    public SplitType getSplitType() {
        return SplitType.SHARE;
    }

    @Override
    public Map<Long, BigDecimal> calculateSplits(BigDecimal totalAmount, List<SplitParticipantItem> participants) {
        if (participants == null || participants.isEmpty()) {
            throw new BadRequestException("Participants list cannot be empty for share-based split");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total amount must be greater than zero");
        }

        BigDecimal scaledTotal = MoneyUtils.round(totalAmount);
        BigDecimal totalShares = BigDecimal.ZERO;

        for (SplitParticipantItem p : participants) {
            if (p.getSplitValue() == null || p.getSplitValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Share count must be greater than zero for user " + p.getUserId());
            }
            totalShares = totalShares.add(p.getSplitValue());
        }

        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal totalAllocated = BigDecimal.ZERO;
        List<ParticipantRemainder> remainders = new ArrayList<>();

        for (SplitParticipantItem p : participants) {
            BigDecimal exactShare = scaledTotal.multiply(p.getSplitValue()).divide(totalShares, 4, RoundingMode.HALF_EVEN);
            BigDecimal flooredShare = exactShare.setScale(2, RoundingMode.FLOOR);
            BigDecimal remainderFraction = exactShare.subtract(flooredShare);

            result.put(p.getUserId(), flooredShare);
            totalAllocated = totalAllocated.add(flooredShare);
            remainders.add(new ParticipantRemainder(p.getUserId(), remainderFraction));
        }

        // Sort descending by highest remainder fraction to distribute pennies fairly
        remainders.sort((a, b) -> b.remainderFraction.compareTo(a.remainderFraction));

        int penniesToDistribute = scaledTotal.subtract(totalAllocated).movePointRight(2).intValueExact();
        for (int i = 0; i < penniesToDistribute; i++) {
            Long userId = remainders.get(i % remainders.size()).userId;
            result.put(userId, result.get(userId).add(ONE_CENT));
        }

        return result;
    }

    private record ParticipantRemainder(Long userId, BigDecimal remainderFraction) {}
}

