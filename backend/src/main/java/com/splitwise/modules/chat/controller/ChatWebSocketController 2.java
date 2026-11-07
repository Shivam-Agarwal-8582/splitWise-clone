package com.splitwise.modules.chat.controller;

import com.splitwise.modules.chat.dto.ChatMessageRequest;
import com.splitwise.modules.chat.dto.TypingIndicatorDTO;
import com.splitwise.modules.chat.service.ChatService;
import com.splitwise.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/groups/{groupId}/chat")
    public void handleGroupMessage(
            @DestinationVariable Long groupId,
            @Payload ChatMessageRequest request,
            Principal principal) {
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof CustomUserDetails user) {
            chatService.sendMessage(groupId, user.getId(), request);
        }
    }

    @MessageMapping("/groups/{groupId}/typing")
    public void handleTyping(
            @DestinationVariable Long groupId,
            @Payload TypingIndicatorDTO typingDto,
            Principal principal) {
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof CustomUserDetails user) {
            chatService.broadcastTyping(groupId, user.getId(), typingDto.isTyping());
        }
    }

    @MessageMapping("/groups/{groupId}/read")
    public void handleReadReceipt(
            @DestinationVariable Long groupId,
            @Payload Long messageId,
            Principal principal) {
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof CustomUserDetails user) {
            chatService.markMessageAsRead(groupId, messageId, user.getId());
        }
    }
}

