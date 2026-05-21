package com.esun.financialsystem.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PostFavoriteProductRequest(
        @NotBlank String userId,
        @NotNull @Positive Long productNo,
        @NotNull @Positive Integer purchaseQuantity,
        @NotBlank String account) {
}
