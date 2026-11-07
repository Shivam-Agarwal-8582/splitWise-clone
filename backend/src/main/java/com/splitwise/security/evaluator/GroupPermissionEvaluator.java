package com.splitwise.security.evaluator;

import com.splitwise.modules.group.model.GroupRole;
import com.splitwise.modules.group.repository.GroupMemberRepository;
import com.splitwise.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("groupPermissionEvaluator")
@RequiredArgsConstructor
public class GroupPermissionEvaluator {

    private final GroupMemberRepository groupMemberRepository;

    public boolean isMember(Authentication authentication, Long groupId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userDetails.getId());
    }

    public boolean isAdmin(Authentication authentication, Long groupId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }
        return groupMemberRepository.existsByGroupIdAndUserIdAndRole(groupId, userDetails.getId(), GroupRole.ADMIN);
    }
}

