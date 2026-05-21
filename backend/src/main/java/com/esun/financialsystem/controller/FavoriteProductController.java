package com.esun.financialsystem.controller;

import com.esun.financialsystem.dto.request.AddFavoriteProductRequest;
import com.esun.financialsystem.dto.request.UpdateFavoriteProductRequest;
import com.esun.financialsystem.dto.response.DeleteFavoriteProductResponse;
import com.esun.financialsystem.dto.response.FavoriteProductMutationResponse;
import com.esun.financialsystem.dto.response.FavoriteProductResponse;
import com.esun.financialsystem.service.FavoriteProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/favorite-products")
public class FavoriteProductController {

    private final FavoriteProductService favoriteProductService;

    public FavoriteProductController(FavoriteProductService favoriteProductService) {
        this.favoriteProductService = favoriteProductService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteProductMutationResponse addFavoriteProduct(
            @Valid @RequestBody AddFavoriteProductRequest request) {
        long sn = favoriteProductService.addFavoriteProduct(request);
        return new FavoriteProductMutationResponse(sn, "Favorite product created");
    }

    @GetMapping("/users/{userId}")
    public List<FavoriteProductResponse> getFavoriteProductsByUser(@PathVariable String userId) {
        return favoriteProductService.getFavoriteProductsByUser(userId);
    }

    @PutMapping("/{sn}")
    public FavoriteProductMutationResponse updateFavoriteProduct(
            @PathVariable long sn,
            @Valid @RequestBody UpdateFavoriteProductRequest request) {
        long updatedSn = favoriteProductService.updateFavoriteProduct(sn, request);
        return new FavoriteProductMutationResponse(updatedSn, "Favorite product updated");
    }

    @DeleteMapping("/{sn}")
    public DeleteFavoriteProductResponse deleteFavoriteProduct(@PathVariable long sn) {
        boolean deleted = favoriteProductService.deleteFavoriteProduct(sn);
        return new DeleteFavoriteProductResponse(deleted);
    }
}
