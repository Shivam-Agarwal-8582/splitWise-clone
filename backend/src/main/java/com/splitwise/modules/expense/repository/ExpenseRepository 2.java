package com.splitwise.modules.expense.repository;

import com.splitwise.modules.expense.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId AND e.isDeleted = false ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findAllActiveByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId AND e.isDeleted = false ORDER BY e.expenseDate DESC, e.createdAt DESC")
    Page<Expense> findPageActiveByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    Optional<Expense> findByIdAndIsDeletedFalse(Long id);
}

