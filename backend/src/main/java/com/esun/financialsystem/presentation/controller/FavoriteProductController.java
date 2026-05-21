package com.esun.financialsystem.presentation.controller;


// request：API 請求的參數物件
import com.esun.financialsystem.presentation.request.PostFavoriteProductRequest;
import com.esun.financialsystem.presentation.request.GetLikeListRequest;
import com.esun.financialsystem.presentation.request.PutFavoriteProductRequest;


// response：API 回傳的 JSON 物件
import com.esun.financialsystem.presentation.response.DeleteFavoriteProductResponse;
import com.esun.financialsystem.presentation.response.FavoriteProductMutationResponse;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import com.esun.financialsystem.presentation.response.PagedResponse;

// business service：商業邏輯層
import com.esun.financialsystem.business.service.FavoriteProductService;



import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
// @Validated
// 啟用參數驗證功能
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
// @RestController
// 告訴 Spring 這是一個 REST API Controller
// 回傳的內容會自動轉成 JSON
import org.springframework.web.bind.annotation.RestController;

// Controller：類似 Express router/controller
// 專門負責：
// 1. 接 API Request
// 2. 接收參數
// 3. 呼叫 Service
// 4. 回傳 JSON

// Spring API Flow：
// Request
// ↓
// Controller（接 API）
// ↓
// Service（商業邏輯）
// ↓
// Repository（查 DB）
// ↓
// Database
// ↓
// Response(JSON)
@Validated
@RestController



// API Base Route
// 類似：app.use("/api/favorite-products", router)
@RequestMapping("/api/favorite-products")
public class FavoriteProductController {

    // Service：商業邏輯層
    // Controller 不直接操作 DB
    private final FavoriteProductService favoriteProductService;

    // Spring 自動注入 Service
    // 類似：const service = new FavoriteProductService()
    public FavoriteProductController(FavoriteProductService favoriteProductService) {
        this.favoriteProductService = favoriteProductService;  // 將 Spring 注入進來的 Service 存到 class 成員變數
    }

    // 查詢喜好商品清單
    // GET /api/favorite-products/like-list
    @GetMapping("/like-list")
    public PagedResponse<LikeListResponse> getLikeList(@Valid @ModelAttribute GetLikeListRequest request) {
        // 將 request 丟給下一層 Service
        return favoriteProductService.getLikeList(request);
    }

    // 新增喜好商品
    // POST /api/favorite-products
    // @RequestBody 代表接收 JSON body
    // @Valid 代表自動進行欄位驗證
    // 回傳 HTTP 201 Created
    // 類似 router.post("/")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteProductMutationResponse postFavoriteProduct(
            // 類似 req.body
            @Valid @RequestBody PostFavoriteProductRequest request) {
        // 呼叫 Service 執行新增邏輯
        long sn = favoriteProductService.postFavoriteProduct(request);
        // 類似 res.json({...})
        return new FavoriteProductMutationResponse(sn, "Favorite product created");
    }

    // 查詢指定使用者的喜好商品
    // GET /api/favorite-products/users/{userId}
    // @PathVariable 取得 URL 路徑參數
    // 類似 router.get("/users/:userId")
    @GetMapping("/users/{userId}")
    public List<FavoriteProductResponse> getFavoriteProductsByUser(
            // 類似 req.params.userId
            @PathVariable String userId) {
        return favoriteProductService.getFavoriteProductsByUser(userId);
    }

    // 更新喜好商品
    // PUT /api/favorite-products/{sn}
    // 類似 router.put("/:sn")
    @PutMapping("/{sn}")
    public FavoriteProductMutationResponse putFavoriteProduct(
            @PathVariable long sn,
            @Valid @RequestBody PutFavoriteProductRequest request) {
        long updatedSn = favoriteProductService.putFavoriteProduct(sn, request);
        return new FavoriteProductMutationResponse(updatedSn, "Favorite product updated");
    }

    // 刪除喜好商品
    // DELETE /api/favorite-products/{sn}
    // 類似 router.delete("/:sn")
    @DeleteMapping("/{sn}")
    public DeleteFavoriteProductResponse deleteFavoriteProduct(@PathVariable long sn) {
        // 呼叫 Service 執行刪除邏輯
        boolean deleted = favoriteProductService.deleteFavoriteProduct(sn);
        return new DeleteFavoriteProductResponse(deleted);
    }
}
