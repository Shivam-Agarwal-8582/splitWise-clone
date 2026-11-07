package com.splitwise.modules.settlement.service;

import com.splitwise.common.utils.MoneyUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DebtSimplificationService {

    public record DebtResult(Long debtorId, Long creditorId, BigDecimal amount) {}

    public record UserBalanceNode(Long userId, BigDecimal balance) {}

    /**
     * Min-Cash-Flow Greedy Max-Heap Algorithm.
     * Takes a map of userId -> netBalance (positive = creditor, negative = debtor).
     * Returns a minimized list of transactions to resolve all debts.
     * Time Complexity: O(K log K) where K is number of users with non-zero balances.
     * Space Complexity: O(K) for heaps and result list.
     */
    public List<DebtResult> simplifyDebts(Map<Long, BigDecimal> netBalances) {
        List<DebtResult> results = new ArrayList<>();
        if (netBalances == null || netBalances.isEmpty()) {
            return results;
        }

        // Max-heaps: creditors sorted by balance descending, debtors sorted by absolute debt descending
        PriorityQueue<UserBalanceNode> creditors = new PriorityQueue<>(
                (a, b) -> b.balance().compareTo(a.balance())
        );
        PriorityQueue<UserBalanceNode> debtors = new PriorityQueue<>(
                (a, b) -> b.balance().compareTo(a.balance()) // absolute values
        );

        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            BigDecimal bal = MoneyUtils.round(entry.getValue());
            if (bal.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new UserBalanceNode(entry.getKey(), bal));
            } else if (bal.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new UserBalanceNode(entry.getKey(), bal.abs()));
            }
        }

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            UserBalanceNode maxCreditor = creditors.poll();
            UserBalanceNode maxDebtor = debtors.poll();

            BigDecimal settlementAmount = maxCreditor.balance().min(maxDebtor.balance());

            if (settlementAmount.compareTo(BigDecimal.ZERO) > 0) {
                results.add(new DebtResult(
                        maxDebtor.userId(),
                        maxCreditor.userId(),
                        settlementAmount
                ));
            }

            BigDecimal creditorRemaining = maxCreditor.balance().subtract(settlementAmount);
            BigDecimal debtorRemaining = maxDebtor.balance().subtract(settlementAmount);

            if (creditorRemaining.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new UserBalanceNode(maxCreditor.userId(), creditorRemaining));
            }
            if (debtorRemaining.compareTo(BigDecimal.ZERO) > 0) {
                debtors.add(new UserBalanceNode(maxDebtor.userId(), debtorRemaining));
            }
        }

        return results;
    }
}

