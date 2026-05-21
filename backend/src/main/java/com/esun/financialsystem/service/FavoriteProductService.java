package com.esun.financialsystem.service;

import com.esun.financialsystem.dto.request.AddFavoriteProductRequest;
import com.esun.financialsystem.dto.request.UpdateFavoriteProductRequest;
import com.esun.financialsystem.dto.response.FavoriteProductResponse;
import java.util.List;

public interface FavoriteProductService {

    long addFavoriteProduct(AddFavoriteProductRequest request);

    List<FavoriteProductResponse> getFavoriteProductsByUser(String userId);

    long updateFavoriteProduct(long sn, UpdateFavoriteProductRequest request);

    boolean deleteFavoriteProduct(long sn);
}
