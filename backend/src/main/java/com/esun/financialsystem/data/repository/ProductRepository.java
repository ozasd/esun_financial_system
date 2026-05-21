package com.esun.financialsystem.data.repository;

import com.esun.financialsystem.presentation.request.GetProductRequest;
import com.esun.financialsystem.presentation.response.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<ProductResponse> getProduct(GetProductRequest request);

    long countProduct(GetProductRequest request);

    Optional<ProductResponse> getProductById(long no);

    long postProduct(String productName, BigDecimal price, BigDecimal feeRate);

    Optional<Long> putProduct(long no, String productName, BigDecimal price, BigDecimal feeRate);

    boolean deleteProduct(long no);
}
