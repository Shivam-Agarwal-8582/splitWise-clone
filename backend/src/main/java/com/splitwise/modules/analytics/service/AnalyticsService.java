package com.splitwise.modules.analytics.service;

import com.splitwise.common.utils.MoneyUtils;
import com.splitwise.modules.analytics.dto.GroupAnalyticsDTO;
import com.splitwise.modules.analytics.dto.MonthlySpendingDTO;
import com.splitwise.modules.analytics.dto.SpendingByCategoryDTO;
import com.splitwise.modules.expense.model.Expense;
import com.splitwise.modules.expense.model.ExpenseCategory;
import com.splitwise.modules.expense.repository.ExpenseRepository;
import com.splitwise.modules.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Transactional(readOnly = true)
    public GroupAnalyticsDTO getGroupAnalytics(Long groupId, Long userId) {
        groupService.assertUserIsMember(groupId, userId);

        List<Expense> expenses = expenseRepository.findAllActiveByGroupId(groupId);

        BigDecimal total = BigDecimal.ZERO;
        Map<ExpenseCategory, BigDecimal> categorySums = new EnumMap<>(ExpenseCategory.class);
        Map<String, BigDecimal> monthlySums = new TreeMap<>();

        for (Expense e : expenses) {
            BigDecimal amt = MoneyUtils.round(e.getTotalAmount());
            total = total.add(amt);

            categorySums.put(e.getCategory(),
                    categorySums.getOrDefault(e.getCategory(), BigDecimal.ZERO).add(amt));

            String monthStr = e.getExpenseDate().format(MONTH_FORMATTER);
            monthlySums.put(monthStr,
                    monthlySums.getOrDefault(monthStr, BigDecimal.ZERO).add(amt));
        }

        final BigDecimal finalTotal = total;

        List<SpendingByCategoryDTO> categoryBreakdown = categorySums.entrySet().stream()
                .map(entry -> {
                    double pct = finalTotal.compareTo(BigDecimal.ZERO) > 0 ?
                            entry.getValue().multiply(BigDecimal.valueOf(100))
                                    .divide(finalTotal, 2, RoundingMode.HALF_EVEN).doubleValue()
                            : 0.0;
                    return SpendingByCategoryDTO.builder()
                            .category(entry.getKey())
                            .amount(entry.getValue())
                            .percentage(pct)
                            .build();
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());

        List<MonthlySpendingDTO> monthlyTrends = monthlySums.entrySet().stream()
                .map(entry -> MonthlySpendingDTO.builder()
                        .month(entry.getKey())
                        .totalAmount(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return GroupAnalyticsDTO.builder()
                .groupId(groupId)
                .totalSpending(MoneyUtils.round(total))
                .categoryBreakdown(categoryBreakdown)
                .monthlyTrends(monthlyTrends)
                .build();
    }
}

