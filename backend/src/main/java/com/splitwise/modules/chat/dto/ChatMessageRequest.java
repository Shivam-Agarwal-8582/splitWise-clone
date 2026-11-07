package com.splitwise.modules.chat.dto;

import com.splitwise.modules.chat.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMessageRequest {

    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
    private String content;

    private MessageType messageType = MessageType.TEXT;
    private String mediaUrl;
}

