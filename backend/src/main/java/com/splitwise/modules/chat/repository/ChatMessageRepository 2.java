package com.splitwise.modules.chat.repository;

import com.splitwise.modules.chat.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.group.id = :groupId AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<ChatMessage> findAllActiveByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT m FROM ChatMessage m WHERE m.group.id = :groupId AND m.isDeleted = false ORDER BY m.createdAt DESC")
    Page<ChatMessage> findPagedActiveByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    Optional<ChatMessage> findByIdAndIsDeletedFalse(Long id);
}

