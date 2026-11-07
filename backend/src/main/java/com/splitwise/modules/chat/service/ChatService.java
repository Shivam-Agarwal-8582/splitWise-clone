package com.splitwise.modules.chat.service;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.common.exception.ForbiddenException;
import com.splitwise.common.exception.ResourceNotFoundException;
import com.splitwise.modules.chat.dto.ChatMessageRequest;
import com.splitwise.modules.chat.dto.ChatMessageResponse;
import com.splitwise.modules.chat.dto.ReadReceiptDTO;
import com.splitwise.modules.chat.dto.TypingIndicatorDTO;
import com.splitwise.modules.chat.model.ChatMessage;
import com.splitwise.modules.chat.model.MessageRead;
import com.splitwise.modules.chat.model.MessageType;
import com.splitwise.modules.chat.repository.ChatMessageRepository;
import com.splitwise.modules.chat.repository.MessageReadRepository;
import com.splitwise.modules.group.model.Group;
import com.splitwise.modules.group.model.GroupRole;
import com.splitwise.modules.group.repository.GroupMemberRepository;
import com.splitwise.modules.group.repository.GroupRepository;
import com.splitwise.modules.group.service.GroupService;
import com.splitwise.modules.user.model.User;
import com.splitwise.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MessageReadRepository messageReadRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse sendMessage(Long groupId, Long senderId, ChatMessageRequest request) {
        groupService.assertUserIsMember(groupId, senderId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));

        ChatMessage message = ChatMessage.builder()
                .group(group)
                .sender(sender)
                .messageType(request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT)
                .content(request.getContent().trim())
                .mediaUrl(request.getMediaUrl())
                .readReceipts(new ArrayList<>())
                .build();

        message = chatMessageRepository.save(message);
        ChatMessageResponse response = ChatMessageResponse.fromEntity(message);

        // Broadcast to group chat topic
        messagingTemplate.convertAndSend("/topic/groups/" + groupId + "/chat", response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getGroupChatHistory(Long groupId, Long userId) {
        groupService.assertUserIsMember(groupId, userId);
        return chatMessageRepository.findAllActiveByGroupId(groupId).stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getGroupChatHistoryPaged(Long groupId, Long userId, Pageable pageable) {
        groupService.assertUserIsMember(groupId, userId);
        return chatMessageRepository.findPagedActiveByGroupId(groupId, pageable)
                .map(ChatMessageResponse::fromEntity);
    }

    @Transactional
    public ChatMessageResponse editMessage(Long groupId, Long messageId, Long userId, String newContent) {
        groupService.assertUserIsMember(groupId, userId);
        ChatMessage message = chatMessageRepository.findByIdAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));

        if (!message.getSender().getId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own messages");
        }

        message.setContent(newContent.trim());
        message.setEdited(true);
        message = chatMessageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.fromEntity(message);
        messagingTemplate.convertAndSend("/topic/groups/" + groupId + "/chat", response);
        return response;
    }

    @Transactional
    public void deleteMessage(Long groupId, Long messageId, Long userId) {
        groupService.assertUserIsMember(groupId, userId);
        ChatMessage message = chatMessageRepository.findByIdAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));

        boolean isSender = message.getSender().getId().equals(userId);
        boolean isAdmin = groupMemberRepository.existsByGroupIdAndUserIdAndRole(groupId, userId, GroupRole.ADMIN);

        if (!isSender && !isAdmin) {
            throw new ForbiddenException("You do not have permission to delete this message");
        }

        message.setDeleted(true);
        chatMessageRepository.save(message);

        // Notify group of deletion
        messagingTemplate.convertAndSend("/topic/groups/" + groupId + "/chat.delete", messageId);
    }

    public void broadcastTyping(Long groupId, Long userId, boolean isTyping) {
        groupService.assertUserIsMember(groupId, userId);
        User user = userRepository.findById(userId).orElse(null);
        String name = user != null ? user.getFullName() : "Someone";

        TypingIndicatorDTO typing = TypingIndicatorDTO.builder()
                .groupId(groupId)
                .userId(userId)
                .userName(name)
                .isTyping(isTyping)
                .build();

        messagingTemplate.convertAndSend("/topic/groups/" + groupId + "/typing", typing);
    }

    @Transactional
    public void markMessageAsRead(Long groupId, Long messageId, Long userId) {
        groupService.assertUserIsMember(groupId, userId);

        ChatMessage message = chatMessageRepository.findByIdAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!messageReadRepository.existsByMessageIdAndUserId(messageId, userId)) {
            MessageRead read = MessageRead.builder()
                    .message(message)
                    .user(user)
                    .build();
            messageReadRepository.save(read);
        }

        ReadReceiptDTO receipt = ReadReceiptDTO.builder()
                .groupId(groupId)
                .messageId(messageId)
                .userId(userId)
                .readAt(Instant.now())
                .build();

        messagingTemplate.convertAndSend("/topic/groups/" + groupId + "/read", receipt);
    }
}

