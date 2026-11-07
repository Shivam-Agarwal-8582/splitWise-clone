package com.splitwise.modules.group.controller;

import com.splitwise.common.response.ApiResponse;
import com.splitwise.modules.group.dto.AddMemberRequest;
import com.splitwise.modules.group.dto.CreateGroupRequest;
import com.splitwise.modules.group.dto.GroupMemberDTO;
import com.splitwise.modules.group.dto.GroupResponseDTO;
import com.splitwise.modules.group.service.GroupService;
import com.splitwise.security.user.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponseDTO>> createGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateGroupRequest request) {
        GroupResponseDTO response = groupService.createGroup(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponseDTO>>> getUserGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GroupResponseDTO> groups = groupService.getUserGroups(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponseDTO>> getGroupDetails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        GroupResponseDTO response = groupService.getGroupDetails(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponseDTO>> updateGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupRequest request) {
        GroupResponseDTO response = groupService.updateGroup(groupId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", response));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        groupService.deleteGroup(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully", null));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberDTO>>> getGroupMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        List<GroupMemberDTO> members = groupService.getGroupMembers(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupMemberDTO>> addMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request) {
        GroupMemberDTO member = groupService.addMember(groupId, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", member));
    }

    @DeleteMapping("/{groupId}/members/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long targetUserId) {
        groupService.removeMember(groupId, userDetails.getId(), targetUserId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<ApiResponse<GroupResponseDTO>> joinByInviteCode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String inviteCode) {
        GroupResponseDTO group = groupService.joinGroupByInviteCode(inviteCode, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Joined group successfully", group));
    }

    @PostMapping("/{groupId}/regenerate-invite")
    public ResponseEntity<ApiResponse<Map<String, String>>> regenerateInviteCode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        String newCode = groupService.regenerateInviteCode(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("inviteCode", newCode)));
    }
}

