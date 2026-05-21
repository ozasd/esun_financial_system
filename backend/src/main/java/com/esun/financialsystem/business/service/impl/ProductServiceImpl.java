package com.esun.financialsystem.business.service.impl;

import com.esun.financialsystem.business.service.ProductService;
import com.esun.financialsystem.common.exception.BadRequestException;
import com.esun.financialsystem.common.exception.ResourceNotFoundException;
import com.esun.financialsystem.data.repository.ProductRepository;
import com.esun.financialsystem.presentation.request.GetProductRequest;
import com.esun.financialsystem.presentation.request.PostProductRequest;
import com.esun.financialsystem.presentation.request.PutProductRequest;
import com.esun.financialsystem.presentation.response.PagedResponse;
import com.esun.financialsystem.presentation.response.ProductResponse;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Set<String> ALLOWED_SORT_COLUMNS =
            Set.of("no", "product_name", "price", "fee_rate", "created_at", "updated_at");

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public PagedResponse<ProductResponse> getProduct(GetProductRequest request) {
        validatePage(request.getPage());
        validatePageSize(request.getPageSize());
        validateSortBy(request.getSortBy());
        validateSortDirection(request.getSortDirection());
        validateRange(request);

        return new PagedResponse<>(
                productRepository.getProduct(request),
                productRepository.countProduct(request),
                request.getPage() == null ? 1 : request.getPage(),
                request.getPageSize() == null ? 10 : request.getPageSize());
    }

    @Override
    public ProductResponse getProductById(long no) {
        validateNo(no);
        return productRepository.getProductById(no)
                .orElseThrow(() -> new ResourceNotFoundException("Product %s does not exist".formatted(no)));
    }

    @Override
    public long postProduct(PostProductRequest request) {
        return productRepository.postProduct(
                request.productName().trim(),
                request.price(),
                request.feeRate());
    }

    @Override
    public long putProduct(long no, PutProductRequest request) {
        validateNo(no);
        return productRepository.putProduct(
                        no,
                        request.productName().trim(),
                        request.price(),
                        request.feeRate())
                .orElseThrow(() -> new ResourceNotFoundException("Product %s does not exist".formatted(no)));
    }

    @Override
    public boolean deleteProduct(long no) {
        validateNo(no);
        if (!productRepository.deleteProduct(no)) {
            throw new ResourceNotFoundException("Product %s does not exist".formatted(no));
        }
        return true;
    }

    private void validateNo(long no) {
        if (no <= 0) {
            throw new BadRequestException("no must be greater than 0");
        }
    }

    private void validatePage(Integer page) {
        if (page != null && page < 1) {
            throw new BadRequestException("page must be greater than 0");
        }
    }

    private void validatePageSize(Integer pageSize) {
        if (pageSize != null && (pageSize < 1 || pageSize > 100)) {
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
        String normalized = sortDirection.trim().toUpperCase();
        if (!"ASC".equals(normalized) && !"DESC".equals(normalized)) {
            throw new BadRequestException("sortDirection must be ASC or DESC");
        }
    }

    private void validateRange(GetProductRequest request) {
        if (request.getPriceMin() != null && request.getPriceMax() != null
                && request.getPriceMin().compareTo(request.getPriceMax()) > 0) {
            throw new BadRequestException("priceMin must be less than or equal to priceMax");
        }
        if (request.getFeeRateMin() != null && request.getFeeRateMax() != null
                && request.getFeeRateMin().compareTo(request.getFeeRateMax()) > 0) {
            throw new BadRequestException("feeRateMin must be less than or equal to feeRateMax");
        }
    }
}
