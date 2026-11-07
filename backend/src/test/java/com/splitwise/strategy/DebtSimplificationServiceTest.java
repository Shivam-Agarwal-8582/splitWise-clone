package com.splitwise.strategy;

import com.splitwise.modules.settlement.service.DebtSimplificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DebtSimplificationServiceTest {

    private DebtSimplificationService debtService;

    @BeforeEach
    void setUp() {
        debtService = new DebtSimplificationService();
    }

    @Test
    @DisplayName("Scenario 1: Empty balances returns empty transactions")
    void testEmptyBalances() {
        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(Map.of());
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Scenario 2: All zero balances returns empty transactions")
    void testAllZeroBalances() {
        Map<Long, BigDecimal> balances = Map.of(
                1L, BigDecimal.ZERO,
                2L, BigDecimal.ZERO,
                3L, BigDecimal.ZERO
        );
        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Scenario 3: 2 members - A owes B exactly 25.00")
    void testTwoMembersDirectDebt() {
        Map<Long, BigDecimal> balances = Map.of(
                1L, new BigDecimal("-25.00"), // A owes
                2L, new BigDecimal("25.00")   // B receives
        );

        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).debtorId());
        assertEquals(2L, results.get(0).creditorId());
        assertEquals(new BigDecimal("25.00"), results.get(0).amount());
    }

    @Test
    @DisplayName("Scenario 4: Transitive Debt - A owes B 20, B owes C 20 -> A pays C 20 directly")
    void testTransitiveSimplification() {
        // Net: A = -20, B = 0, C = +20
        Map<Long, BigDecimal> balances = Map.of(
                1L, new BigDecimal("-20.00"),
                2L, BigDecimal.ZERO,
                3L, new BigDecimal("20.00")
        );

        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).debtorId());
        assertEquals(3L, results.get(0).creditorId());
        assertEquals(new BigDecimal("20.00"), results.get(0).amount());
    }

    @Test
    @DisplayName("Scenario 5: 3 members circular debt cancels to zero")
    void testCircularDebtCancelsOut() {
        // If A paid for B, B paid for C, C paid for A equally -> all net balances are 0
        Map<Long, BigDecimal> balances = Map.of(
                1L, new BigDecimal("0.00"),
                2L, new BigDecimal("0.00"),
                3L, new BigDecimal("0.00")
        );

        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Scenario 6: 1 creditor, 2 debtors")
    void testOneCreditorTwoDebtors() {
        // Creditor 1: +60, Debtor 2: -40, Debtor 3: -20
        Map<Long, BigDecimal> balances = Map.of(
                1L, new BigDecimal("60.00"),
                2L, new BigDecimal("-40.00"),
                3L, new BigDecimal("-20.00")
        );

        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);
        assertEquals(2, results.size());

        BigDecimal totalSettled = results.stream().map(DebtSimplificationService.DebtResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("60.00"), totalSettled);
    }

    @Test
    @DisplayName("Scenario 7: 2 creditors, 1 debtor")
    void testTwoCreditorsOneDebtor() {
        // Debtor 1: -100, Creditor 2: +70, Creditor 3: +30
        Map<Long, BigDecimal> balances = Map.of(
                1L, new BigDecimal("-100.00"),
                2L, new BigDecimal("70.00"),
                3L, new BigDecimal("30.00")
        );

        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);
        assertEquals(2, results.size());

        BigDecimal totalSettled = results.stream().map(DebtSimplificationService.DebtResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), totalSettled);
    }

    @Test
    @DisplayName("Scenario 8: 4 members - 2 creditors, 2 debtors with matching amounts")
    void testFourMembersMatching() {
        Map<Long, BigDecimal> balances = Map.of(
                1L, new BigDecimal("-50.00"),
                2L, new BigDecimal("-30.00"),
                3L, new BigDecimal("50.00"),
                4L, new BigDecimal("30.00")
        );

        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);
        assertEquals(2, results.size());

        BigDecimal totalSettled = results.stream().map(DebtSimplificationService.DebtResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("80.00"), totalSettled);
    }

    @Test
    @DisplayName("Scenario 9: Complex 5 members trip settlement reduces transactions")
    void testFiveMembersComplexTrip() {
        // 5 members on a trip:
        // Alice: +120.00, Bob: +45.00, Charlie: -75.00, Dave: -60.00, Eve: -30.00
        // Sum creditors = 165.00, Sum debtors = 165.00
        Map<Long, BigDecimal> balances = Map.of(
                1L, new BigDecimal("120.00"),
                2L, new BigDecimal("45.00"),
                3L, new BigDecimal("-75.00"),
                4L, new BigDecimal("-60.00"),
                5L, new BigDecimal("-30.00")
        );

        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);

        // At most K - 1 = 4 transactions (often fewer!)
        assertTrue(results.size() <= 4);

        BigDecimal totalSettled = results.stream().map(DebtSimplificationService.DebtResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("165.00"), totalSettled);
    }

    @Test
    @DisplayName("Scenario 10: Fractions and odd pennies handled precisely")
    void testOddPenniesPrecision() {
        Map<Long, BigDecimal> balances = Map.of(
                1L, new BigDecimal("33.34"),
                2L, new BigDecimal("-16.67"),
                3L, new BigDecimal("-16.67")
        );

        List<DebtSimplificationService.DebtResult> results = debtService.simplifyDebts(balances);
        assertEquals(2, results.size());

        BigDecimal totalSettled = results.stream().map(DebtSimplificationService.DebtResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("33.34"), totalSettled);
    }
}
