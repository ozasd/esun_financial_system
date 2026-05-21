package com.esun.financialsystem.presentation.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FavoriteProductSummaryResponse(
        Long sn,
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
