package com.splitwise.modules.expense.controller;

import com.splitwise.common.response.ApiResponse;
import com.splitwise.modules.expense.dto.CreateExpenseRequest;
import com.splitwise.modules.expense.dto.ExpenseResponseDTO;
import com.splitwise.modules.expense.service.ExpenseService;
import com.splitwise.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> createExpense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request) {
        ExpenseResponseDTO response = expenseService.createExpense(groupId, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponseDTO>>> getGroupExpenses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        List<ExpenseResponseDTO> expenses = expenseService.getGroupExpenses(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<Page<ExpenseResponseDTO>>> getGroupExpensesPaged(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ExpenseResponseDTO> expenses = expenseService.getGroupExpensesPaged(
                groupId, userDetails.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate", "createdAt")));
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> getExpenseDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long expenseId) {
        ExpenseResponseDTO response = expenseService.getExpenseDetails(groupId, expenseId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> updateExpense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @Valid @RequestBody CreateExpenseRequest request) {
        ExpenseResponseDTO response = expenseService.updateExpense(groupId, expenseId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long expenseId) {
        expenseService.deleteExpense(groupId, expenseId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }
}

