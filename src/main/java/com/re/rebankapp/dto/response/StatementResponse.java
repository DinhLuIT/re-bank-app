package com.re.rebankapp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.re.rebankapp.enums.TransactionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementResponse {

    private String transactionCode;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private TransactionType type;

    // Ignore khi trả về JSON, chỉ dùng để tính toán nội bộ
    @JsonIgnore
    private Long fromAccountId;
    @JsonIgnore
    private Long toAccountId;

    // JPQL Constructor Projection
    public StatementResponse(String transactionCode, BigDecimal amount, String description, LocalDateTime createdAt, Long fromAccountId, Long toAccountId) {
        this.transactionCode = transactionCode;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
    }
}
