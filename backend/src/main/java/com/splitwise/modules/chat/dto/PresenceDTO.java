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
public class PresenceDTO {
    private Long groupId;
    private Long userId;
    private String status; // "ONLINE", "OFFLINE"
    private Instant timestamp;
}

