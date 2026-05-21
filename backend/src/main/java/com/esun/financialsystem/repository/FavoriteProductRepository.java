package com.esun.financialsystem.repository;

import com.esun.financialsystem.dto.response.FavoriteProductResponse;
import java.util.List;

public interface FavoriteProductRepository {

    long addFavoriteProduct(String userId, long productNo, int purchaseQuantity, String account);

    List<FavoriteProductResponse> getFavoriteProductsByUser(String userId);

    long updateFavoriteProduct(long sn, long productNo, int purchaseQuantity, String account);

    boolean deleteFavoriteProduct(long sn);
}
