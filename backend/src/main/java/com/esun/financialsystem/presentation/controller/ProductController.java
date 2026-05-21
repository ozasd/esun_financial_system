package com.esun.financialsystem.presentation.controller;

import com.esun.financialsystem.business.service.ProductService;
import com.esun.financialsystem.presentation.request.GetProductRequest;
import com.esun.financialsystem.presentation.request.PostProductRequest;
import com.esun.financialsystem.presentation.request.PutProductRequest;
import com.esun.financialsystem.presentation.response.DeleteProductResponse;
import com.esun.financialsystem.presentation.response.PagedResponse;
import com.esun.financialsystem.presentation.response.ProductMutationResponse;
import com.esun.financialsystem.presentation.response.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PagedResponse<ProductResponse> getProduct(@Valid @ModelAttribute GetProductRequest request) {
        return productService.getProduct(request);
    }

    @GetMapping("/{no}")
    public ProductResponse getProductById(@PathVariable long no) {
        return productService.getProductById(no);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductMutationResponse postProduct(@Valid @RequestBody PostProductRequest request) {
        long no = productService.postProduct(request);
        return new ProductMutationResponse(no, "Product created");
    }

    @PutMapping("/{no}")
    public ProductMutationResponse putProduct(
            @PathVariable long no,
            @Valid @RequestBody PutProductRequest request) {
        long updatedNo = productService.putProduct(no, request);
        return new ProductMutationResponse(updatedNo, "Product updated");
    }

    @DeleteMapping("/{no}")
    public DeleteProductResponse deleteProduct(@PathVariable long no) {
        return new DeleteProductResponse(productService.deleteProduct(no));
    }
}
