package com.esun.financialsystem.presentation.response;

import java.util.List;

public record LikeListResponse(
        String userId,
        String userName,
        String email,
        String account,
        List<FavoriteProductSummaryResponse> favoriteProducts) {
}
