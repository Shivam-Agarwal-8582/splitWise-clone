package com.splitwise.modules.group.service;

import com.splitwise.common.exception.BadRequestException;
import com.splitwise.common.exception.ConflictException;
import com.splitwise.common.exception.ForbiddenException;
import com.splitwise.common.exception.ResourceNotFoundException;
import com.splitwise.modules.group.dto.AddMemberRequest;
import com.splitwise.modules.group.dto.CreateGroupRequest;
import com.splitwise.modules.group.dto.GroupMemberDTO;
import com.splitwise.modules.group.dto.GroupResponseDTO;
import com.splitwise.modules.group.model.Group;
import com.splitwise.modules.group.model.GroupMember;
import com.splitwise.modules.group.model.GroupRole;
import com.splitwise.modules.group.repository.GroupMemberRepository;
import com.splitwise.modules.group.repository.GroupRepository;
import com.splitwise.modules.user.model.User;
import com.splitwise.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponseDTO createGroup(Long currentUserId, CreateGroupRequest request) {
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        String inviteCode = generateUniqueInviteCode();

        Group group = Group.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .groupType(request.getGroupType())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .avatarUrl(request.getAvatarUrl())
                .simplifyDebtsDefault(request.isSimplifyDebtsDefault())
                .inviteCode(inviteCode)
                .createdBy(creator)
                .members(new ArrayList<>())
                .build();

        // Add creator as ADMIN
        GroupMember creatorMember = GroupMember.builder()
                .group(group)
                .user(creator)
                .role(GroupRole.ADMIN)
                .build();
        group.getMembers().add(creatorMember);

        // Add initial members if provided
        if (request.getInitialMemberUserIds() != null) {
            for (Long memberId : request.getInitialMemberUserIds()) {
                if (!memberId.equals(currentUserId)) {
                    userRepository.findById(memberId).ifPresent(user -> {
                        GroupMember member = GroupMember.builder()
                                .group(group)
                                .user(user)
                                .role(GroupRole.MEMBER)
                                .build();
                        group.getMembers().add(member);
                    });
                }
            }
        }

        Group savedGroup = groupRepository.save(group);
        return GroupResponseDTO.fromEntity(savedGroup);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDTO> getUserGroups(Long userId) {
        return groupRepository.findAllByUserId(userId).stream()
                .map(GroupResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponseDTO getGroupDetails(Long groupId, Long userId) {
        assertUserIsMember(groupId, userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        return GroupResponseDTO.fromEntity(group);
    }

    @Transactional
    public GroupResponseDTO updateGroup(Long groupId, Long currentUserId, CreateGroupRequest request) {
        assertUserIsAdmin(groupId, currentUserId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        group.setName(request.getName().trim());
        if (request.getDescription() != null) group.setDescription(request.getDescription());
        if (request.getGroupType() != null) group.setGroupType(request.getGroupType());
        if (request.getCurrency() != null) group.setCurrency(request.getCurrency());
        if (request.getAvatarUrl() != null) group.setAvatarUrl(request.getAvatarUrl());
        group.setSimplifyDebtsDefault(request.isSimplifyDebtsDefault());

        Group savedGroup = groupRepository.save(group);
        return GroupResponseDTO.fromEntity(savedGroup);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long currentUserId) {
        assertUserIsAdmin(groupId, currentUserId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        groupRepository.delete(group);
    }

    @Transactional
    public GroupMemberDTO addMember(Long groupId, Long currentUserId, AddMemberRequest request) {
        assertUserIsAdmin(groupId, currentUserId);
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getUserId())) {
            throw new ConflictException("User is already a member of this group");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(request.getRole() != null ? request.getRole() : GroupRole.MEMBER)
                .build();

        member = groupMemberRepository.save(member);
        return GroupMemberDTO.fromEntity(member);
    }

    @Transactional
    public void removeMember(Long groupId, Long currentUserId, Long targetUserId) {
        assertUserIsAdmin(groupId, currentUserId);
        if (currentUserId.equals(targetUserId)) {
            throw new BadRequestException("Group admin cannot remove themselves. Reassign admin or delete group.");
        }
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)) {
            throw new ResourceNotFoundException("Member not found in group");
        }
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Transactional
    public GroupResponseDTO joinGroupByInviteCode(String inviteCode, Long currentUserId) {
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation code"));

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), currentUserId)) {
            return GroupResponseDTO.fromEntity(group);
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupRole.MEMBER)
                .build();

        groupMemberRepository.save(member);
        group.getMembers().add(member);
        return GroupResponseDTO.fromEntity(group);
    }

    @Transactional
    public String regenerateInviteCode(Long groupId, Long currentUserId) {
        assertUserIsAdmin(groupId, currentUserId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        String newCode = generateUniqueInviteCode();
        group.setInviteCode(newCode);
        groupRepository.save(group);
        return newCode;
    }

    @Transactional(readOnly = true)
    public List<GroupMemberDTO> getGroupMembers(Long groupId, Long currentUserId) {
        assertUserIsMember(groupId, currentUserId);
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(GroupMemberDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void assertUserIsMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ForbiddenException("You are not a member of this group");
        }
    }

    public void assertUserIsAdmin(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserIdAndRole(groupId, userId, GroupRole.ADMIN)) {
            throw new ForbiddenException("Only group admins have permission for this action");
        }
    }

    private String generateUniqueInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
