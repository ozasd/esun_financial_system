package com.esun.financialsystem.business.service;

import com.esun.financialsystem.presentation.request.AddFavoriteProductRequest;
import com.esun.financialsystem.presentation.request.UpdateFavoriteProductRequest;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import java.util.List;

public interface FavoriteProductService {

    long addFavoriteProduct(AddFavoriteProductRequest request);

    List<FavoriteProductResponse> getFavoriteProductsByUser(String userId);

    long updateFavoriteProduct(long sn, UpdateFavoriteProductRequest request);

    boolean deleteFavoriteProduct(long sn);
}
