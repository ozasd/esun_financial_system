package com.esun.financialsystem.business.service.impl;

import com.esun.financialsystem.presentation.request.PostFavoriteProductRequest;
import com.esun.financialsystem.presentation.request.GetLikeListRequest;
import com.esun.financialsystem.presentation.request.PutFavoriteProductRequest;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import com.esun.financialsystem.presentation.response.PagedResponse;
import com.esun.financialsystem.common.exception.BadRequestException;
import com.esun.financialsystem.data.repository.FavoriteProductRepository;
import com.esun.financialsystem.business.service.FavoriteProductService;
import java.util.Set;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FavoriteProductServiceImpl implements FavoriteProductService {

    private static final Set<String> ALLOWED_SORT_COLUMNS =
            Set.of("user_id", "user_name", "email", "account");

    private final FavoriteProductRepository favoriteProductRepository;

    public FavoriteProductServiceImpl(FavoriteProductRepository favoriteProductRepository) {
        this.favoriteProductRepository = favoriteProductRepository;
    }

    @Override
    public long postFavoriteProduct(PostFavoriteProductRequest request) {
        validateUserId(request.userId());
        validateAccount(request.account());
        return favoriteProductRepository.postFavoriteProduct(
                request.userId(),
                request.productNo(),
                request.purchaseQuantity(),
                request.account());
    }

    @Override
    public PagedResponse<LikeListResponse> getLikeList(GetLikeListRequest request) {
        int page = request.getPage() == null ? 1 : request.getPage();
        int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();

        validatePage(page);
        validatePageSize(pageSize);
        validateSortBy(request.getSortBy());
        validateSortDirection(request.getSortDirection());

        List<LikeListResponse> datas = favoriteProductRepository.getLikeList(request);
        long total = favoriteProductRepository.countLikeList(request);
        return new PagedResponse<>(datas, total, page, pageSize);
    }

    @Override
    public List<FavoriteProductResponse> getFavoriteProductsByUser(String userId) {
        validateUserId(userId);
        return favoriteProductRepository.getFavoriteProductsByUser(userId);
    }

    @Override
    public long putFavoriteProduct(long sn, PutFavoriteProductRequest request) {
        validateAccount(request.account());
        return favoriteProductRepository.putFavoriteProduct(
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

    private void validatePage(int page) {
        if (page < 1) {
            throw new BadRequestException("page must be greater than 0");
        }
    }

    private void validatePageSize(int pageSize) {
        if (pageSize < 1 || pageSize > 100) {
            throw new BadRequestException("pageSize must be between 1 and 100");
        }
    }

    private void validateSortBy(String sortBy) {
        if (StringUtils.hasText(sortBy) && !ALLOWED_SORT_COLUMNS.contains(sortBy.trim())) {
            throw new BadRequestException("sortBy is not supported");
        }
    }

    private void validateSortDirection(String sortDirection) {
        if (!StringUtils.hasText(sortDirection)) {
            return;
        }
        String normalizedDirection = sortDirection.trim().toUpperCase();
        if (!"ASC".equals(normalizedDirection) && !"DESC".equals(normalizedDirection)) {
            throw new BadRequestException("sortDirection must be ASC or DESC");
        }
    }
}
