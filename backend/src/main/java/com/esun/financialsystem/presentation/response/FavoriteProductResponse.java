package com.esun.financialsystem.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FavoriteProductResponse(
        Long sn,
        String userId,
        String userName,
        String email,
        Long productNo,
        String productName,
        BigDecimal price,
        BigDecimal feeRate,
        Integer purchaseQuantity,
        String account,
        BigDecimal totalFee,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
