package com.splitwise.modules.analytics.controller;

import com.splitwise.common.response.ApiResponse;
import com.splitwise.modules.analytics.dto.GroupAnalyticsDTO;
import com.splitwise.modules.analytics.service.AnalyticsService;
import com.splitwise.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<ApiResponse<GroupAnalyticsDTO>> getGroupAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        GroupAnalyticsDTO analytics = analyticsService.getGroupAnalytics(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}

