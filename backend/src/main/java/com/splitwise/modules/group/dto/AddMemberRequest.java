package com.splitwise.modules.group.dto;

import com.splitwise.modules.group.model.GroupRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    private GroupRole role = GroupRole.MEMBER;
}
