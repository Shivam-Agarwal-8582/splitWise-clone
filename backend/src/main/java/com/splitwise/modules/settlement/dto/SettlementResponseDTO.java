package com.splitwise.modules.settlement.dto;

import com.splitwise.modules.settlement.model.Settlement;
import com.splitwise.modules.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponseDTO {
    private Long id;
    private Long groupId;
    private UserDTO payer;
    private UserDTO payee;
    private BigDecimal amount;
    private String currency;
    private Instant settlementDate;
    private String notes;

    public static SettlementResponseDTO fromEntity(Settlement settlement) {
        if (settlement == null) return null;
        return SettlementResponseDTO.builder()
                .id(settlement.getId())
                .groupId(settlement.getGroup().getId())
                .payer(UserDTO.fromEntity(settlement.getPayer()))
                .payee(UserDTO.fromEntity(settlement.getPayee()))
                .amount(settlement.getAmount())
                .currency(settlement.getCurrency())
                .settlementDate(settlement.getSettlementDate())
                .notes(settlement.getNotes())
                .build();
    }
}

