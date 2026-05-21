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

// @Service
// 告訴 Spring：這是一個 Service 層物件
// 類似 Express 的 service module
@Service
// Service Impl（真正商業邏輯）
// interface 是功能規格
// impl 才是真正執行內容
public class FavoriteProductServiceImpl implements FavoriteProductService {

    // 允許排序的欄位
    // 避免前端亂傳 SQL 欄位造成 SQL Injection
    private static final Set<String> ALLOWED_SORT_COLUMNS =
            Set.of("user_id", "user_name", "email", "account");

    // Repository：負責資料庫操作
    // 類似 Express 的 model/db layer
    private final FavoriteProductRepository favoriteProductRepository;

    // Spring 自動注入 Repository
    public FavoriteProductServiceImpl(FavoriteProductRepository favoriteProductRepository) {
        this.favoriteProductRepository = favoriteProductRepository;
    }

    // 新增商品商業邏輯
    // 1. 驗證參數
    // 2. 呼叫 Repository
    // 3. Repository 再去操作 DB
    @Override
    public long postFavoriteProduct(PostFavoriteProductRequest request) {
        // 驗證 userId
        validateUserId(request.userId());
        validateAccount(request.account());
        // 呼叫 Repository 寫入 DB
        return favoriteProductRepository.postFavoriteProduct(
                request.userId(),
                request.productNo(),
                request.purchaseQuantity(),
                request.account());
    }

    // 查詢清單商業邏輯
    // 類似：service.getList(req.query)
    @Override
    public PagedResponse<LikeListResponse> getLikeList(GetLikeListRequest request) {
        int page = request.getPage() == null ? 1 : request.getPage();
        int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();

        validatePage(page);
        validatePageSize(pageSize);
        validateSortBy(request.getSortBy());
        validateSortDirection(request.getSortDirection());

        // Repository 查詢 DB 資料
        List<LikeListResponse> datas = favoriteProductRepository.getLikeList(request);
        long total = favoriteProductRepository.countLikeList(request);
        // 組合分頁資料後回傳給 Controller
        return new PagedResponse<>(datas, total, page, pageSize);
    }

    @Override
    public List<FavoriteProductResponse> getFavoriteProductsByUser(String userId) {
        validateUserId(userId);
        return favoriteProductRepository.getFavoriteProductsByUser(userId);
    }

    // 更新商品商業邏輯
    @Override
    public long putFavoriteProduct(long sn, PutFavoriteProductRequest request) {
        validateAccount(request.account());
        return favoriteProductRepository.putFavoriteProduct(
                sn,
                request.productNo(),
                request.purchaseQuantity(),
                request.account());
    }

    // 刪除商品商業邏輯
    @Override
    public boolean deleteFavoriteProduct(long sn) {
        return favoriteProductRepository.deleteFavoriteProduct(sn);
    }

    // 驗證邏輯
    // Service 很常集中處理商業驗證
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
