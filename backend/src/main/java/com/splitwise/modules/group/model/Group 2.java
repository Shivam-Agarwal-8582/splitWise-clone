package com.splitwise.modules.group.model;

import com.splitwise.common.audit.BaseAuditableEntity;
import com.splitwise.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups", indexes = {
        @Index(name = "idx_groups_creator", columnList = "created_by")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupType groupType = GroupType.OTHER;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(length = 500)
    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean simplifyDebtsDefault = true;

    @Column(length = 20, unique = true)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();
}

