package com.splitwise.modules.group.repository;

import com.splitwise.modules.group.model.GroupMember;
import com.splitwise.modules.group.model.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndRole(Long groupId, Long userId, GroupRole role);

    List<GroupMember> findByGroupId(Long groupId);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}

