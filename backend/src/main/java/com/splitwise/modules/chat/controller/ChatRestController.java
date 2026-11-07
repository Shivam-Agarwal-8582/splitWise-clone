package com.splitwise.modules.chat.controller;

import com.splitwise.common.response.ApiResponse;
import com.splitwise.modules.chat.dto.ChatMessageRequest;
import com.splitwise.modules.chat.dto.ChatMessageResponse;
import com.splitwise.modules.chat.service.ChatService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(groupId, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId) {
        List<ChatMessageResponse> messages = chatService.getGroupChatHistory(groupId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/messages/paged")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessagesPaged(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ChatMessageResponse> messages = chatService.getGroupChatHistoryPaged(
                groupId, userDetails.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> editMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body) {
        String newContent = body.get("content");
        ChatMessageResponse response = chatService.editMessage(groupId, messageId, userDetails.getId(), newContent);
        return ResponseEntity.ok(ApiResponse.success("Message edited successfully", response));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long messageId) {
        chatService.deleteMessage(groupId, messageId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long messageId) {
        chatService.markMessageAsRead(groupId, messageId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", null));
    }
}

