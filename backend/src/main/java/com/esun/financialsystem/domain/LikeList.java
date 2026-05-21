package com.esun.financialsystem.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LikeList(
        Long sn,
        Integer purchaseQuantity,
        String account,
        BigDecimal totalFee,
        BigDecimal totalAmount,
        String userId,
        Long productNo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
