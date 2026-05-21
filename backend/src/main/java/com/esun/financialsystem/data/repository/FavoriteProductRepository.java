package com.esun.financialsystem.data.repository;

import com.esun.financialsystem.presentation.request.GetLikeListRequest;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import java.util.List;

public interface FavoriteProductRepository {

    long postFavoriteProduct(String userId, long productNo, int purchaseQuantity, String account);

    List<LikeListResponse> getLikeList(GetLikeListRequest request);

    long countLikeList(GetLikeListRequest request);

    List<FavoriteProductResponse> getFavoriteProductsByUser(String userId);

    long putFavoriteProduct(long sn, long productNo, int purchaseQuantity, String account);

    boolean deleteFavoriteProduct(long sn);
}
