package com.esun.financialsystem.business.service.impl;

import com.esun.financialsystem.presentation.request.AddFavoriteProductRequest;
import com.esun.financialsystem.presentation.request.UpdateFavoriteProductRequest;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import com.esun.financialsystem.common.exception.BadRequestException;
import com.esun.financialsystem.data.repository.FavoriteProductRepository;
import com.esun.financialsystem.business.service.FavoriteProductService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FavoriteProductServiceImpl implements FavoriteProductService {

    private final FavoriteProductRepository favoriteProductRepository;

    public FavoriteProductServiceImpl(FavoriteProductRepository favoriteProductRepository) {
        this.favoriteProductRepository = favoriteProductRepository;
    }

    @Override
    public long addFavoriteProduct(AddFavoriteProductRequest request) {
        validateUserId(request.userId());
        validateAccount(request.account());
        return favoriteProductRepository.addFavoriteProduct(
                request.userId(),
                request.productNo(),
                request.purchaseQuantity(),
                request.account());
    }

    @Override
    public List<FavoriteProductResponse> getFavoriteProductsByUser(String userId) {
        validateUserId(userId);
        return favoriteProductRepository.getFavoriteProductsByUser(userId);
    }

    @Override
    public long updateFavoriteProduct(long sn, UpdateFavoriteProductRequest request) {
        validateAccount(request.account());
        return favoriteProductRepository.updateFavoriteProduct(
                sn,
                request.productNo(),
                request.purchaseQuantity(),
                request.account());
    }

    @Override
    public boolean deleteFavoriteProduct(long sn) {
        return favoriteProductRepository.deleteFavoriteProduct(sn);
    }

    private void validateUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BadRequestException("userId must not be blank");
        }
    }

    private void validateAccount(String account) {
        if (!StringUtils.hasText(account)) {
            throw new BadRequestException("account must not be blank");
        }
    }
}
