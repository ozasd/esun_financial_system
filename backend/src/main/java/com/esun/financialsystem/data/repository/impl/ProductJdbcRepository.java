package com.esun.financialsystem.data.repository.impl;

import com.esun.financialsystem.data.mapper.ProductRowMapper;
import com.esun.financialsystem.data.repository.ProductRepository;
import com.esun.financialsystem.data.sql.ProductSql;
import com.esun.financialsystem.presentation.request.GetProductRequest;
import com.esun.financialsystem.presentation.response.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class ProductJdbcRepository implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ProductRowMapper productRowMapper;

    public ProductJdbcRepository(JdbcTemplate jdbcTemplate, ProductRowMapper productRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.productRowMapper = productRowMapper;
    }

    @Override
    public List<ProductResponse> getProduct(GetProductRequest request) {
        return jdbcTemplate.query(
                ProductSql.GET_PRODUCTS,
                productRowMapper,
                request.getNo(),
                trimToNull(request.getProductName()),
                trimToNull(request.getKeyword()),
                request.getPriceMin(),
                request.getPriceMax(),
                request.getFeeRateMin(),
                request.getFeeRateMax(),
                resolveSortBy(request),
                resolveSortDirection(request),
                resolvePageSize(request),
                resolveOffset(request));
    }

    @Override
    public long countProduct(GetProductRequest request) {
        Long total = jdbcTemplate.queryForObject(
                ProductSql.COUNT_PRODUCTS,
                Long.class,
                request.getNo(),
                trimToNull(request.getProductName()),
                trimToNull(request.getKeyword()),
                request.getPriceMin(),
                request.getPriceMax(),
                request.getFeeRateMin(),
                request.getFeeRateMax());
        return total == null ? 0L : total;
    }

    @Override
    public Optional<ProductResponse> getProductById(long no) {
        List<ProductResponse> products = jdbcTemplate.query(ProductSql.GET_PRODUCT_BY_ID, productRowMapper, no);
        return products.stream().findFirst();
    }

    @Override
    public long postProduct(String productName, BigDecimal price, BigDecimal feeRate) {
        Long no = jdbcTemplate.queryForObject(ProductSql.ADD_PRODUCT, Long.class, productName, price, feeRate);
        return no == null ? 0L : no;
    }

    @Override
    public Optional<Long> putProduct(long no, String productName, BigDecimal price, BigDecimal feeRate) {
        Long updatedNo = jdbcTemplate.queryForObject(
                ProductSql.UPDATE_PRODUCT,
                Long.class,
                no,
                productName,
                price,
                feeRate);
        return Optional.ofNullable(updatedNo);
    }

    @Override
    public boolean deleteProduct(long no) {
        Long deletedNo = jdbcTemplate.queryForObject(
                ProductSql.DELETE_PRODUCT,
                Long.class,
                no);
        return deletedNo != null;
    }

    private int resolveOffset(GetProductRequest request) {
        return (resolvePage(request) - 1) * resolvePageSize(request);
    }

    private int resolvePage(GetProductRequest request) {
        return request.getPage() == null ? 1 : request.getPage();
    }

    private int resolvePageSize(GetProductRequest request) {
        return request.getPageSize() == null ? 10 : request.getPageSize();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String resolveSortBy(GetProductRequest request) {
        String sortBy = trimToNull(request.getSortBy());
        return sortBy == null ? "no" : sortBy;
    }

    private String resolveSortDirection(GetProductRequest request) {
        String sortDirection = trimToNull(request.getSortDirection());
        return "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
    }
}
