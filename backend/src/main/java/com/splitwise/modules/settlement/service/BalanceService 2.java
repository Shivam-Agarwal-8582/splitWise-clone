package com.splitwise.modules.settlement.service;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.common.exception.ResourceNotFoundException;
import com.splitwise.common.utils.MoneyUtils;
import com.splitwise.modules.expense.model.Expense;
import com.splitwise.modules.expense.model.ExpensePayer;
import com.splitwise.modules.expense.model.ExpenseSplit;
import com.splitwise.modules.expense.repository.ExpenseRepository;
import com.splitwise.modules.group.model.Group;
import com.splitwise.modules.group.model.GroupMember;
import com.splitwise.modules.group.repository.GroupMemberRepository;
import com.splitwise.modules.group.repository.GroupRepository;
import com.splitwise.modules.group.service.GroupService;
import com.splitwise.modules.settlement.dto.*;
import com.splitwise.modules.settlement.model.Settlement;
import com.splitwise.modules.settlement.repository.SettlementRepository;
import com.splitwise.modules.user.dto.UserDTO;
import com.splitwise.modules.user.model.User;
import com.splitwise.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final DebtSimplificationService debtSimplificationService;

    @Transactional(readOnly = true)
    public GroupBalanceSummaryDTO calculateGroupBalances(Long groupId, Long currentUserId) {
        groupService.assertUserIsMember(groupId, currentUserId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        List<Expense> expenses = expenseRepository.findAllActiveByGroupId(groupId);
        List<Settlement> settlements = settlementRepository.findByGroupIdOrderBySettlementDateDesc(groupId);

        Map<Long, User> userMap = members.stream()
                .collect(Collectors.toMap(m -> m.getUser().getId(), GroupMember::getUser, (a, b) -> a));

        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> owedMap = new HashMap<>();
        Map<Long, BigDecimal> settlementPaidMap = new HashMap<>();
        Map<Long, BigDecimal> settlementReceivedMap = new HashMap<>();

        for (GroupMember m : members) {
            Long uid = m.getUser().getId();
            paidMap.put(uid, BigDecimal.ZERO);
            owedMap.put(uid, BigDecimal.ZERO);
            settlementPaidMap.put(uid, BigDecimal.ZERO);
            settlementReceivedMap.put(uid, BigDecimal.ZERO);
        }

        BigDecimal totalGroupSpending = BigDecimal.ZERO;

        // Sum expenses
        for (Expense expense : expenses) {
            totalGroupSpending = totalGroupSpending.add(expense.getTotalAmount());

            for (ExpensePayer payer : expense.getPayers()) {
                Long uid = payer.getUser().getId();
                paidMap.put(uid, paidMap.getOrDefault(uid, BigDecimal.ZERO).add(payer.getAmountPaid()));
            }

            for (ExpenseSplit split : expense.getSplits()) {
                Long uid = split.getUser().getId();
                owedMap.put(uid, owedMap.getOrDefault(uid, BigDecimal.ZERO).add(split.getOwedAmount()));
            }
        }

        // Sum settlements
        for (Settlement settlement : settlements) {
            Long payerId = settlement.getPayer().getId();
            Long payeeId = settlement.getPayee().getId();

            settlementPaidMap.put(payerId, settlementPaidMap.getOrDefault(payerId, BigDecimal.ZERO).add(settlement.getAmount()));
            settlementReceivedMap.put(payeeId, settlementReceivedMap.getOrDefault(payeeId, BigDecimal.ZERO).add(settlement.getAmount()));
        }

        // Calculate net balance for each member
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        List<MemberBalanceDTO> memberBalanceList = new ArrayList<>();

        for (GroupMember m : members) {
            Long uid = m.getUser().getId();
            BigDecimal totalPaid = MoneyUtils.round(paidMap.getOrDefault(uid, BigDecimal.ZERO));
            BigDecimal totalOwed = MoneyUtils.round(owedMap.getOrDefault(uid, BigDecimal.ZERO));
            BigDecimal sPaid = MoneyUtils.round(settlementPaidMap.getOrDefault(uid, BigDecimal.ZERO));
            BigDecimal sReceived = MoneyUtils.round(settlementReceivedMap.getOrDefault(uid, BigDecimal.ZERO));

            // Net Balance = (Total Paid - Total Owed) + (Settlement Paid - Settlement Received)
            BigDecimal netBalance = totalPaid.subtract(totalOwed).add(sPaid).subtract(sReceived);
            netBalance = MoneyUtils.round(netBalance);
            netBalances.put(uid, netBalance);

            memberBalanceList.add(MemberBalanceDTO.builder()
                    .user(UserDTO.fromEntity(userMap.get(uid)))
                    .totalPaid(totalPaid)
                    .totalOwed(totalOwed)
                    .netBalance(netBalance)
                    .build());
        }

        // Run debt simplification
        List<DebtSimplificationService.DebtResult> simplified = debtSimplificationService.simplifyDebts(netBalances);
        List<SimplifiedDebtDTO> simplifiedDebtDTOs = simplified.stream()
                .map(d -> SimplifiedDebtDTO.builder()
                        .debtor(UserDTO.fromEntity(userMap.get(d.debtorId())))
                        .creditor(UserDTO.fromEntity(userMap.get(d.creditorId())))
                        .amount(d.amount())
                        .currency(group.getCurrency())
                        .build())
                .collect(Collectors.toList());

        return GroupBalanceSummaryDTO.builder()
                .groupId(groupId)
                .currency(group.getCurrency())
                .totalGroupSpending(MoneyUtils.round(totalGroupSpending))
                .memberBalances(memberBalanceList)
                .simplifiedDebts(simplifiedDebtDTOs)
                .build();
    }

    @Transactional
    public SettlementResponseDTO recordSettlement(Long groupId, Long currentUserId, SettlementRequest request) {
        groupService.assertUserIsMember(groupId, currentUserId);
        groupService.assertUserIsMember(groupId, request.getPayerId());
        groupService.assertUserIsMember(groupId, request.getPayeeId());

        if (request.getPayerId().equals(request.getPayeeId())) {
            throw new BadRequestException("Payer and payee cannot be the same person");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        User payer = userRepository.findById(request.getPayerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getPayerId()));
        User payee = userRepository.findById(request.getPayeeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getPayeeId()));

        Settlement settlement = Settlement.builder()
                .group(group)
                .payer(payer)
                .payee(payee)
                .amount(MoneyUtils.round(request.getAmount()))
                .currency(request.getCurrency() != null ? request.getCurrency() : group.getCurrency())
                .notes(request.getNotes())
                .build();

        settlement = settlementRepository.save(settlement);
        return SettlementResponseDTO.fromEntity(settlement);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponseDTO> getGroupSettlements(Long groupId, Long currentUserId) {
        groupService.assertUserIsMember(groupId, currentUserId);
        return settlementRepository.findByGroupIdOrderBySettlementDateDesc(groupId).stream()
                .map(SettlementResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

