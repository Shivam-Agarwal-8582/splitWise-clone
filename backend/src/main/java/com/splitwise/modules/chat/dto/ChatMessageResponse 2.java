package com.splitwise.modules.chat.dto;

import com.splitwise.modules.chat.model.ChatMessage;
import com.splitwise.modules.chat.model.MessageType;
import com.splitwise.modules.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long groupId;
    private UserDTO sender;
    private MessageType messageType;
    private String content;
    private String mediaUrl;
    private boolean isEdited;
    private Instant createdAt;
    private List<Long> readByUserIds;

    public static ChatMessageResponse fromEntity(ChatMessage msg) {
        if (msg == null) return null;
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .groupId(msg.getGroup().getId())
                .sender(UserDTO.fromEntity(msg.getSender()))
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .mediaUrl(msg.getMediaUrl())
                .isEdited(msg.isEdited())
                .createdAt(msg.getCreatedAt())
                .readByUserIds(msg.getReadReceipts() != null ?
                        msg.getReadReceipts().stream().map(r -> r.getUser().getId()).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}

