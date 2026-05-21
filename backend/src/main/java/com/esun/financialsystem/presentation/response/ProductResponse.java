package com.esun.financialsystem.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long no,
        String productName,
        BigDecimal price,
        BigDecimal feeRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
