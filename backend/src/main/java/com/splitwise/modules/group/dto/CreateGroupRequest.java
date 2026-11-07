package com.splitwise.modules.group.dto;

import com.splitwise.modules.group.model.GroupType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String name;

    private String description;
    private GroupType groupType = GroupType.OTHER;
    private String currency = "USD";
    private String avatarUrl;
    private boolean simplifyDebtsDefault = true;
    private List<Long> initialMemberUserIds;
}

