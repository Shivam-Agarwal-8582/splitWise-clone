package com.splitwise.modules.expense.service;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.common.exception.ResourceNotFoundException;
import com.splitwise.common.utils.MoneyUtils;
import com.splitwise.modules.expense.dto.CreateExpenseRequest;
import com.splitwise.modules.expense.dto.ExpensePayerDTO;
import com.splitwise.modules.expense.dto.ExpenseResponseDTO;
import com.splitwise.modules.expense.dto.ExpenseSplitDTO;
import com.splitwise.modules.expense.model.Expense;
import com.splitwise.modules.expense.model.ExpensePayer;
import com.splitwise.modules.expense.model.ExpenseSplit;
import com.splitwise.modules.expense.repository.ExpensePayerRepository;
import com.splitwise.modules.expense.repository.ExpenseRepository;
import com.splitwise.modules.expense.repository.ExpenseSplitRepository;
import com.splitwise.modules.expense.strategy.ExpenseSplitStrategy;
import com.splitwise.modules.expense.strategy.ExpenseSplitStrategyFactory;
import com.splitwise.modules.expense.strategy.SplitParticipantItem;
import com.splitwise.modules.group.model.Group;
import com.splitwise.modules.group.repository.GroupMemberRepository;
import com.splitwise.modules.group.repository.GroupRepository;
import com.splitwise.modules.group.service.GroupService;
import com.splitwise.modules.user.model.User;
import com.splitwise.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpensePayerRepository expensePayerRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final ExpenseSplitStrategyFactory strategyFactory;

    @Transactional
    public ExpenseResponseDTO createExpense(Long groupId, Long currentUserId, CreateExpenseRequest request) {
        groupService.assertUserIsMember(groupId, currentUserId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        BigDecimal totalAmount = MoneyUtils.round(request.getTotalAmount());
        validatePayersAndSplitsMembership(groupId, request.getPayers(), request.getSplits());
        validatePayersTotal(totalAmount, request.getPayers());

        ExpenseSplitStrategy strategy = strategyFactory.getStrategy(request.getSplitType());
        List<SplitParticipantItem> participantItems = request.getSplits().stream()
                .map(s -> new SplitParticipantItem(s.getUserId(), s.getSplitValue()))
                .collect(Collectors.toList());

        Map<Long, BigDecimal> calculatedSplits = strategy.calculateSplits(totalAmount, participantItems);

        Expense expense = Expense.builder()
                .group(group)
                .description(request.getDescription().trim())
                .category(request.getCategory())
                .totalAmount(totalAmount)
                .currency(request.getCurrency() != null ? request.getCurrency() : group.getCurrency())
                .splitType(request.getSplitType())
                .expenseDate(request.getExpenseDate())
                .createdBy(creator)
                .payers(new ArrayList<>())
                .splits(new ArrayList<>())
                .build();

        // Save payers
        for (ExpensePayerDTO payerDto : request.getPayers()) {
            User payerUser = userRepository.findById(payerDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", payerDto.getUserId()));
            ExpensePayer payer = ExpensePayer.builder()
                    .expense(expense)
                    .user(payerUser)
                    .amountPaid(MoneyUtils.round(payerDto.getAmountPaid()))
                    .build();
            expense.getPayers().add(payer);
        }

        // Save splits
        for (ExpenseSplitDTO splitDto : request.getSplits()) {
            User splitUser = userRepository.findById(splitDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", splitDto.getUserId()));
            BigDecimal owed = calculatedSplits.get(splitDto.getUserId());
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(expense)
                    .user(splitUser)
                    .owedAmount(owed)
                    .splitValue(splitDto.getSplitValue())
                    .build();
            expense.getSplits().add(split);
        }

        expense = expenseRepository.save(expense);
        return ExpenseResponseDTO.fromEntity(expense);
    }

    @Transactional
    public ExpenseResponseDTO updateExpense(Long groupId, Long expenseId, Long currentUserId, CreateExpenseRequest request) {
        groupService.assertUserIsMember(groupId, currentUserId);

        Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        if (!expense.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Expense does not belong to specified group");
        }

        BigDecimal totalAmount = MoneyUtils.round(request.getTotalAmount());
        validatePayersAndSplitsMembership(groupId, request.getPayers(), request.getSplits());
        validatePayersTotal(totalAmount, request.getPayers());

        ExpenseSplitStrategy strategy = strategyFactory.getStrategy(request.getSplitType());
        List<SplitParticipantItem> participantItems = request.getSplits().stream()
                .map(s -> new SplitParticipantItem(s.getUserId(), s.getSplitValue()))
                .collect(Collectors.toList());

        Map<Long, BigDecimal> calculatedSplits = strategy.calculateSplits(totalAmount, participantItems);

        expense.setDescription(request.getDescription().trim());
        expense.setCategory(request.getCategory());
        expense.setTotalAmount(totalAmount);
        if (request.getCurrency() != null) expense.setCurrency(request.getCurrency());
        expense.setSplitType(request.getSplitType());
        expense.setExpenseDate(request.getExpenseDate());

        // Replace payers
        expense.getPayers().clear();
        for (ExpensePayerDTO payerDto : request.getPayers()) {
            User payerUser = userRepository.findById(payerDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", payerDto.getUserId()));
            ExpensePayer payer = ExpensePayer.builder()
                    .expense(expense)
                    .user(payerUser)
                    .amountPaid(MoneyUtils.round(payerDto.getAmountPaid()))
                    .build();
            expense.getPayers().add(payer);
        }

        // Replace splits
        expense.getSplits().clear();
        for (ExpenseSplitDTO splitDto : request.getSplits()) {
            User splitUser = userRepository.findById(splitDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", splitDto.getUserId()));
            BigDecimal owed = calculatedSplits.get(splitDto.getUserId());
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(expense)
                    .user(splitUser)
                    .owedAmount(owed)
                    .splitValue(splitDto.getSplitValue())
                    .build();
            expense.getSplits().add(split);
        }

        expense = expenseRepository.save(expense);
        return ExpenseResponseDTO.fromEntity(expense);
    }

    @Transactional
    public void deleteExpense(Long groupId, Long expenseId, Long currentUserId) {
        groupService.assertUserIsMember(groupId, currentUserId);
        Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        if (!expense.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Expense does not belong to specified group");
        }

        expense.setDeleted(true);
        expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseDetails(Long groupId, Long expenseId, Long currentUserId) {
        groupService.assertUserIsMember(groupId, currentUserId);
        Expense expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        if (!expense.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Expense does not belong to specified group");
        }

        return ExpenseResponseDTO.fromEntity(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getGroupExpenses(Long groupId, Long currentUserId) {
        groupService.assertUserIsMember(groupId, currentUserId);
        return expenseRepository.findAllActiveByGroupId(groupId).stream()
                .map(ExpenseResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDTO> getGroupExpensesPaged(Long groupId, Long currentUserId, Pageable pageable) {
        groupService.assertUserIsMember(groupId, currentUserId);
        return expenseRepository.findPageActiveByGroupId(groupId, pageable)
                .map(ExpenseResponseDTO::fromEntity);
    }

    private void validatePayersAndSplitsMembership(Long groupId, List<ExpensePayerDTO> payers, List<ExpenseSplitDTO> splits) {
        for (ExpensePayerDTO p : payers) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, p.getUserId())) {
                throw new BadRequestException("Payer with ID " + p.getUserId() + " is not a member of the group");
            }
        }
        for (ExpenseSplitDTO s : splits) {
            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, s.getUserId())) {
                throw new BadRequestException("Split participant with ID " + s.getUserId() + " is not a member of the group");
            }
        }
    }

    private void validatePayersTotal(BigDecimal totalAmount, List<ExpensePayerDTO> payers) {
        BigDecimal sumPaid = BigDecimal.ZERO;
        for (ExpensePayerDTO p : payers) {
            if (p.getAmountPaid() == null || p.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Each payer must contribute an amount greater than zero");
            }
            sumPaid = sumPaid.add(MoneyUtils.round(p.getAmountPaid()));
        }

        if (sumPaid.compareTo(totalAmount) != 0) {
            throw new BadRequestException(String.format(
                    "Total amount paid by payers (%s) must equal the total expense amount (%s)",
                    sumPaid, totalAmount
            ));
        }
    }
}

