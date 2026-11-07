package com.splitwise.modules.expense.repository;

import com.splitwise.modules.expense.model.ExpensePayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpensePayerRepository extends JpaRepository<ExpensePayer, Long> {
    List<ExpensePayer> findByExpenseId(Long expenseId);
    void deleteByExpenseId(Long expenseId);
}

