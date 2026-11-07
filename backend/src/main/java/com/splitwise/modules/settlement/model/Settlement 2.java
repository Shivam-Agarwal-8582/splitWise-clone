package com.splitwise.modules.settlement.model;

import com.splitwise.common.audit.BaseAuditableEntity;
import com.splitwise.modules.group.model.Group;
import com.splitwise.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "settlements", indexes = {
        @Index(name = "idx_settle_group", columnList = "group_id"),
        @Index(name = "idx_settle_payer_payee", columnList = "payer_id, payee_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private User payee;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Builder.Default
    @Column(name = "settlement_date", nullable = false)
    private Instant settlementDate = Instant.now();

    @Column(length = 255)
    private String notes;

    @Version
    @Builder.Default
    private Long version = 0L;
}

