package com.splitwise.modules.expense.model;

import com.splitwise.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_splits",
        uniqueConstraints = @UniqueConstraint(name = "uk_expense_split_user", columnNames = {"expense_id", "user_id"}),
        indexes = {
                @Index(name = "idx_es_user", columnList = "user_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "owed_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal owedAmount;

    @Column(name = "split_value", precision = 10, scale = 4)
    private BigDecimal splitValue; // percentage, exact, share value
}

