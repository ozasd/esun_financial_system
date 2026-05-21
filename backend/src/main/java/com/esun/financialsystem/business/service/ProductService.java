package com.esun.financialsystem.business.service;

import com.esun.financialsystem.presentation.request.GetProductRequest;
import com.esun.financialsystem.presentation.request.PostProductRequest;
import com.esun.financialsystem.presentation.request.PutProductRequest;
import com.esun.financialsystem.presentation.response.PagedResponse;
import com.esun.financialsystem.presentation.response.ProductResponse;

public interface ProductService {

    PagedResponse<ProductResponse> getProduct(GetProductRequest request);

    ProductResponse getProductById(long no);

    long postProduct(PostProductRequest request);

    long putProduct(long no, PutProductRequest request);

    boolean deleteProduct(long no);
}
