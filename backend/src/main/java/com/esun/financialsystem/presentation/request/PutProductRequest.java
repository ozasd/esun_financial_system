package com.esun.financialsystem.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PutProductRequest(
        @NotBlank(message = "productName must not be blank")
        String productName,
        @NotNull(message = "price must not be null")
        @DecimalMin(value = "0.0", inclusive = true, message = "price must be greater than or equal to 0")
        BigDecimal price,
        @NotNull(message = "feeRate must not be null")
        @DecimalMin(value = "0.0", inclusive = true, message = "feeRate must be greater than or equal to 0")
        BigDecimal feeRate) {
}
