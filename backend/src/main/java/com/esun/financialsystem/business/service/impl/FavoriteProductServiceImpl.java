package com.esun.financialsystem.business.service.impl;

import com.esun.financialsystem.presentation.request.PostFavoriteProductRequest;
import com.esun.financialsystem.presentation.request.GetLikeListRequest;
import com.esun.financialsystem.presentation.request.PutFavoriteProductRequest
;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import com.esun.financialsystem.presentation.response.PagedResponse;

import com.esun.financialsystem.common.exception.BadRequestException;


import com.esun.financialsystem.data.repository.FavoriteProductRepository;



import com.esun.financialsystem.business.service.FavoriteProductService;
import java.util.Set;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;

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
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Spring 自動注入 Repository
    public FavoriteProductServiceImpl(FavoriteProductRepository favoriteProductRepository,
                                      StringRedisTemplate redisTemplate,
                                      ObjectMapper objectMapper) {
        this.favoriteProductRepository = favoriteProductRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
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

        // Idempotency 檢查
        String idempotencyKey = String.format("idempotency:postFavoriteProduct:%s:%d:%s",
                request.userId(), request.productNo(), request.account());
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofSeconds(10));
        if (Boolean.FALSE.equals(success)) {
            throw new BadRequestException("Duplicate request, please try again later");
        }

        // 將請求轉為 JSON，推入 Redis List (Queue) 以非同步處理
        try {
            String orderJson = objectMapper.writeValueAsString(request);
            redisTemplate.opsForList().leftPush("queue:favorite_products", orderJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to enqueue post favorite product request", e);
        }

        // 提早清除快取 (雖然是最終一致性，但提早清除可以讓後續查詢拿到最新狀態，或者讀到 DB 還沒更新的舊狀態)
        evictUserCache(request.userId());

        // 因為非同步，無法立即得知 DB 生成的 sn，先回傳 0 代表已受理 (Accepted)
        return 0L;
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
        String cacheKey = "cache:user_favorites:" + userId;
        try {
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cachedData)) {
                return objectMapper.readValue(cachedData, new TypeReference<List<FavoriteProductResponse>>() {});
            }
        } catch (Exception e) {
            // Ignore cache read errors and fallback to DB
        }

        List<FavoriteProductResponse> dbResult = favoriteProductRepository.getFavoriteProductsByUser(userId);

        try {
            String jsonData = objectMapper.writeValueAsString(dbResult);
            // 基礎過期時間 5 分鐘 (300秒) + 隨機 0~300 秒，防止快取雪崩
            long ttlSeconds = 300 + ThreadLocalRandom.current().nextInt(301);
            redisTemplate.opsForValue().set(cacheKey, jsonData, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Ignore cache write errors
        }

        return dbResult;
    }

    // 更新商品商業邏輯
    @Override
    public long putFavoriteProduct(long sn, PutFavoriteProductRequest request) {
        validateAccount(request.account());
        String userId = favoriteProductRepository.getUserIdBySn(sn);
        long updatedSn = favoriteProductRepository.putFavoriteProduct(
                sn,
                request.productNo(),
                request.purchaseQuantity(),
                request.account());
        if (userId != null) {
            evictUserCache(userId);
        }
        return updatedSn;
    }

    // 刪除商品商業邏輯
    @Override
    public boolean deleteFavoriteProduct(long sn) {
        String userId = favoriteProductRepository.getUserIdBySn(sn);
        boolean deleted = favoriteProductRepository.deleteFavoriteProduct(sn);
        if (userId != null) {
            evictUserCache(userId);
        }
        return deleted;
    }

    private void evictUserCache(String userId) {
        try {
            redisTemplate.delete("cache:user_favorites:" + userId);
        } catch (Exception e) {
            // Ignore cache eviction errors
        }
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
