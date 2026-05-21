package com.esun.financialsystem.business.worker;

import com.esun.financialsystem.data.repository.FavoriteProductRepository;
import com.esun.financialsystem.presentation.request.PostFavoriteProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@Profile("worker")
public class FavoriteProductWorker {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final FavoriteProductRepository favoriteProductRepository;

    public FavoriteProductWorker(StringRedisTemplate redisTemplate,
                                 ObjectMapper objectMapper,
                                 FavoriteProductRepository favoriteProductRepository) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.favoriteProductRepository = favoriteProductRepository;
    }

    // 每一秒鐘啟動一次，嘗試將 Queue 消化完
    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        while (true) {
            // 從 Redis Queue 右側取出，最多等待 2 秒
            String json = redisTemplate.opsForList().rightPop("queue:favorite_products", 2, TimeUnit.SECONDS);
            
            // 如果取不到資料，跳出迴圈，等下一次排程
            if (!StringUtils.hasText(json)) {
                break;
            }
            
            try {
                // 反序列化
                PostFavoriteProductRequest request = objectMapper.readValue(json, PostFavoriteProductRequest.class);
                
                // 寫入資料庫
                favoriteProductRepository.postFavoriteProduct(
                        request.userId(),
                        request.productNo(),
                        request.purchaseQuantity(),
                        request.account());
                
                // 強制清除快取 (Evict)
                redisTemplate.delete("cache:user_favorites:" + request.userId());
                
                System.out.println("Worker processed order for user: " + request.userId());
            } catch (Exception e) {
                // 實務上這裡可能需要實作 Retry 機制或丟入 Dead-letter queue
                System.err.println("Worker failed to process order: " + json);
                e.printStackTrace();
            }
        }
    }
}
