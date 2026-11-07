package com.splitwise.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptDTO {
    private Long groupId;
    private Long messageId;
    private Long userId;
    private Instant readAt;
}

