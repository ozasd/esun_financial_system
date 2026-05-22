package com.esun.financialsystem.business.service;

import com.esun.financialsystem.presentation.request.PostFavoriteProductRequest;
import com.esun.financialsystem.presentation.request.GetLikeListRequest;
import com.esun.financialsystem.presentation.request.PutFavoriteProductRequest;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import com.esun.financialsystem.presentation.response.PagedResponse;
import java.util.List;

public interface FavoriteProductService {

    long postFavoriteProduct(PostFavoriteProductRequest request, String idempotencyKey);

    PagedResponse<LikeListResponse> getLikeList(GetLikeListRequest request);

    List<FavoriteProductResponse> getFavoriteProductsByUser(String userId);

    long putFavoriteProduct(long sn, PutFavoriteProductRequest request);

    boolean deleteFavoriteProduct(long sn);
}
