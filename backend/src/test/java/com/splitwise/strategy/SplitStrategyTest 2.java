package com.splitwise.strategy;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.modules.expense.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SplitStrategyTest {

    private EqualSplitStrategy equalStrategy;
    private ExactSplitStrategy exactStrategy;
    private PercentageSplitStrategy percentageStrategy;
    private ShareBasedSplitStrategy shareStrategy;

    @BeforeEach
    void setUp() {
        equalStrategy = new EqualSplitStrategy();
        exactStrategy = new ExactSplitStrategy();
        percentageStrategy = new PercentageSplitStrategy();
        shareStrategy = new ShareBasedSplitStrategy();
    }

    @Test
    @DisplayName("Equal Split - 100 divided by 3 distributes remainder penny correctly")
    void testEqualSplitWithPennyRemainder() {
        List<SplitParticipantItem> participants = List.of(
                new SplitParticipantItem(1L, null),
                new SplitParticipantItem(2L, null),
                new SplitParticipantItem(3L, null)
        );

        Map<Long, BigDecimal> splits = equalStrategy.calculateSplits(new BigDecimal("100.00"), participants);

        assertEquals(3, splits.size());
        assertEquals(new BigDecimal("33.34"), splits.get(1L));
        assertEquals(new BigDecimal("33.33"), splits.get(2L));
        assertEquals(new BigDecimal("33.33"), splits.get(3L));

        BigDecimal sum = splits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), sum);
    }

    @Test
    @DisplayName("Equal Split - Exact division leaves no remainder")
    void testEqualSplitExactDivision() {
        List<SplitParticipantItem> participants = List.of(
                new SplitParticipantItem(1L, null),
                new SplitParticipantItem(2L, null)
        );

        Map<Long, BigDecimal> splits = equalStrategy.calculateSplits(new BigDecimal("50.00"), participants);

        assertEquals(new BigDecimal("25.00"), splits.get(1L));
        assertEquals(new BigDecimal("25.00"), splits.get(2L));
    }

    @Test
    @DisplayName("Equal Split - Throws exception when participants list is empty")
    void testEqualSplitEmptyParticipants() {
        assertThrows(BadRequestException.class, () ->
                equalStrategy.calculateSplits(new BigDecimal("50.00"), List.of())
        );
    }

    @Test
    @DisplayName("Exact Split - Valid exact amounts equal total")
    void testExactSplitValid() {
        List<SplitParticipantItem> participants = List.of(
                new SplitParticipantItem(1L, new BigDecimal("45.50")),
                new SplitParticipantItem(2L, new BigDecimal("54.50"))
        );

        Map<Long, BigDecimal> splits = exactStrategy.calculateSplits(new BigDecimal("100.00"), participants);

        assertEquals(new BigDecimal("45.50"), splits.get(1L));
        assertEquals(new BigDecimal("54.50"), splits.get(2L));
    }

    @Test
    @DisplayName("Exact Split - Throws exception when sum does not match total amount")
    void testExactSplitMismatch() {
        List<SplitParticipantItem> participants = List.of(
                new SplitParticipantItem(1L, new BigDecimal("40.00")),
                new SplitParticipantItem(2L, new BigDecimal("50.00"))
        );

        assertThrows(BadRequestException.class, () ->
                exactStrategy.calculateSplits(new BigDecimal("100.00"), participants)
        );
    }

    @Test
    @DisplayName("Percentage Split - Valid 60/40 split")
    void testPercentageSplitValid() {
        List<SplitParticipantItem> participants = List.of(
                new SplitParticipantItem(1L, new BigDecimal("60.00")),
                new SplitParticipantItem(2L, new BigDecimal("40.00"))
        );

        Map<Long, BigDecimal> splits = percentageStrategy.calculateSplits(new BigDecimal("150.00"), participants);

        assertEquals(new BigDecimal("90.00"), splits.get(1L));
        assertEquals(new BigDecimal("60.00"), splits.get(2L));
    }

    @Test
    @DisplayName("Percentage Split - Reconciles penny difference with highest percentage member")
    void testPercentageSplitPennyReconciliation() {
        List<SplitParticipantItem> participants = List.of(
                new SplitParticipantItem(1L, new BigDecimal("33.34")),
                new SplitParticipantItem(2L, new BigDecimal("33.33")),
                new SplitParticipantItem(3L, new BigDecimal("33.33"))
        );

        Map<Long, BigDecimal> splits = percentageStrategy.calculateSplits(new BigDecimal("100.00"), participants);

        BigDecimal sum = splits.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), sum);
    }

    @Test
    @DisplayName("Percentage Split - Throws exception when percentages do not equal 100")
    void testPercentageSplitInvalidSum() {
        List<SplitParticipantItem> participants = List.of(
                new SplitParticipantItem(1L, new BigDecimal("50.00")),
                new SplitParticipantItem(2L, new BigDecimal("40.00"))
        );

        assertThrows(BadRequestException.class, () ->
                percentageStrategy.calculateSplits(new BigDecimal("100.00"), participants)
        );
    }

    @Test
    @DisplayName("Share Split - 2:1:1 ratio distributes 100 into 50, 25, 25")
    void testShareSplitValid() {
        List<SplitParticipantItem> participants = List.of(
                new SplitParticipantItem(1L, new BigDecimal("2")),
                new SplitParticipantItem(2L, new BigDecimal("1")),
                new SplitParticipantItem(3L, new BigDecimal("1"))
        );

        Map<Long, BigDecimal> splits = shareStrategy.calculateSplits(new BigDecimal("100.00"), participants);

        assertEquals(new BigDecimal("50.00"), splits.get(1L));
        assertEquals(new BigDecimal("25.00"), splits.get(2L));
        assertEquals(new BigDecimal("25.00"), splits.get(3L));
    }
}
