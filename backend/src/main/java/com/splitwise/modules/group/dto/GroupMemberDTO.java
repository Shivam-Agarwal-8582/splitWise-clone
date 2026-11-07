package com.splitwise.modules.group.dto;

import com.splitwise.modules.group.model.GroupMember;
import com.splitwise.modules.group.model.GroupRole;
import com.splitwise.modules.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDTO {
    private Long id;
    private UserDTO user;
    private GroupRole role;
    private Instant joinedAt;

    public static GroupMemberDTO fromEntity(GroupMember member) {
        if (member == null) return null;
        return GroupMemberDTO.builder()
                .id(member.getId())
                .user(UserDTO.fromEntity(member.getUser()))
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}

