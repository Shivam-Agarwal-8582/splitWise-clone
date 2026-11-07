package com.splitwise.modules.settlement.controller;

import com.splitwise.common.response.ApiResponse;
import com.splitwise.modules.settlement.dto.GroupBalanceSummaryDTO;
import com.splitwise.modules.settlement.dto.SettlementRequest;
import com.splitwise.modules.settlement.dto.SettlementResponseDTO;
import com.splitwise.modules.settlement.service.BalanceService;
import com.splitwise.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}")
@RequiredArgsConstructor
public class SettlementController {

    private final BalanceService balanceService;

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<GroupBalanceSummaryDTO>> getGroupBalances(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        GroupBalanceSummaryDTO summary = balanceService.calculateGroupBalances(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/settlements")
    public ResponseEntity<ApiResponse<SettlementResponseDTO>> recordSettlement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody SettlementRequest request) {
        SettlementResponseDTO response = balanceService.recordSettlement(groupId, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Settlement recorded successfully", response));
    }

    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<List<SettlementResponseDTO>>> getGroupSettlements(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        List<SettlementResponseDTO> settlements = balanceService.getGroupSettlements(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(settlements));
    }
}

