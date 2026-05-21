package com.esun.financialsystem.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Product(
        Long no,
        String productName,
        BigDecimal price,
        BigDecimal feeRate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
