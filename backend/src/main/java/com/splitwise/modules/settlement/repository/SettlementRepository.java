package com.splitwise.modules.settlement.repository;

import com.splitwise.modules.settlement.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroupIdOrderBySettlementDateDesc(Long groupId);
}

