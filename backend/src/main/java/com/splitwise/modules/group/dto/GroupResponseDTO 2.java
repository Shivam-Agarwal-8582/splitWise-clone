package com.splitwise.modules.group.dto;

import com.splitwise.modules.group.model.Group;
import com.splitwise.modules.group.model.GroupType;
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
public class GroupResponseDTO {
    private Long id;
    private String name;
    private String description;
    private GroupType groupType;
    private String currency;
    private String avatarUrl;
    private boolean simplifyDebtsDefault;
    private String inviteCode;
    private UserDTO createdBy;
    private List<GroupMemberDTO> members;
    private Instant createdAt;

    public static GroupResponseDTO fromEntity(Group group) {
        if (group == null) return null;
        return GroupResponseDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .groupType(group.getGroupType())
                .currency(group.getCurrency())
                .avatarUrl(group.getAvatarUrl())
                .simplifyDebtsDefault(group.isSimplifyDebtsDefault())
                .inviteCode(group.getInviteCode())
                .createdBy(UserDTO.fromEntity(group.getCreatedBy()))
                .members(group.getMembers() != null ?
                        group.getMembers().stream().map(GroupMemberDTO::fromEntity).collect(Collectors.toList())
                        : List.of())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
