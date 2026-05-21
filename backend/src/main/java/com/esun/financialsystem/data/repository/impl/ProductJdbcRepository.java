package com.esun.financialsystem.data.repository.impl;

import com.esun.financialsystem.data.mapper.ProductRowMapper;
import com.esun.financialsystem.data.repository.ProductRepository;
import com.esun.financialsystem.data.sql.ProductSql;
import com.esun.financialsystem.presentation.request.GetProductRequest;
import com.esun.financialsystem.presentation.response.ProductResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        List<Object> parameters = new ArrayList<>();
        String sql = ProductSql.SELECT_PREFIX
                + buildWhereClause(request, parameters)
                + buildOrderClause(request)
                + " LIMIT ? OFFSET ?";
        parameters.add(resolvePageSize(request));
        parameters.add(resolveOffset(request));
        return jdbcTemplate.query(sql, productRowMapper, parameters.toArray());
    }

    @Override
    public long countProduct(GetProductRequest request) {
        List<Object> parameters = new ArrayList<>();
        Long total = jdbcTemplate.queryForObject(
                ProductSql.COUNT_PREFIX + buildWhereClause(request, parameters),
                Long.class,
                parameters.toArray());
        return total == null ? 0L : total;
    }

    @Override
    public Optional<ProductResponse> getProductById(long no) {
        List<ProductResponse> products = jdbcTemplate.query(ProductSql.SELECT_BY_ID, productRowMapper, no);
        return products.stream().findFirst();
    }

    @Override
    public long postProduct(String productName, BigDecimal price, BigDecimal feeRate) {
        Long no = jdbcTemplate.queryForObject(ProductSql.INSERT, Long.class, productName, price, feeRate);
        return no == null ? 0L : no;
    }

    @Override
    public Optional<Long> putProduct(long no, String productName, BigDecimal price, BigDecimal feeRate) {
        List<Long> productNos = jdbcTemplate.query(
                ProductSql.UPDATE,
                (rs, rowNum) -> rs.getLong("no"),
                productName,
                price,
                feeRate,
                no);
        return productNos.stream().findFirst();
    }

    @Override
    public boolean deleteProduct(long no) {
        List<Long> productNos = jdbcTemplate.query(
                ProductSql.DELETE,
                (rs, rowNum) -> rs.getLong("no"),
                no);
        return !productNos.isEmpty();
    }

    private String buildWhereClause(GetProductRequest request, List<Object> parameters) {
        StringBuilder whereClause = new StringBuilder(" WHERE TRUE");
        String productName = trimToNull(request.getProductName());
        String keyword = toKeywordPattern(request.getKeyword());

        if (request.getNo() != null) {
            whereClause.append(" AND p.no = ?");
            parameters.add(request.getNo());
        }
        if (productName != null) {
            whereClause.append(" AND p.product_name ILIKE ?");
            parameters.add("%" + productName + "%");
        }
        if (request.getPriceMin() != null) {
            whereClause.append(" AND p.price >= ?");
            parameters.add(request.getPriceMin());
        }
        if (request.getPriceMax() != null) {
            whereClause.append(" AND p.price <= ?");
            parameters.add(request.getPriceMax());
        }
        if (request.getFeeRateMin() != null) {
            whereClause.append(" AND p.fee_rate >= ?");
            parameters.add(request.getFeeRateMin());
        }
        if (request.getFeeRateMax() != null) {
            whereClause.append(" AND p.fee_rate <= ?");
            parameters.add(request.getFeeRateMax());
        }
        if (keyword != null) {
            whereClause.append("""
                     AND (
                        CAST(p.no AS TEXT) ILIKE ?
                        OR p.product_name ILIKE ?
                    )
                    """);
            parameters.add(keyword);
            parameters.add(keyword);
        }
        return whereClause.toString();
    }

    private String buildOrderClause(GetProductRequest request) {
        Map<String, String> allowedColumns = new LinkedHashMap<>();
        allowedColumns.put("no", "p.no");
        allowedColumns.put("product_name", "p.product_name");
        allowedColumns.put("price", "p.price");
        allowedColumns.put("fee_rate", "p.fee_rate");
        allowedColumns.put("created_at", "p.created_at");
        allowedColumns.put("updated_at", "p.updated_at");

        String orderColumn = allowedColumns.getOrDefault(trimToNull(request.getSortBy()), "p.no");
        String direction = "DESC".equalsIgnoreCase(trimToNull(request.getSortDirection())) ? "DESC" : "ASC";
        return " ORDER BY " + orderColumn + " " + direction;
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

    private String toKeywordPattern(String keyword) {
        return StringUtils.hasText(keyword) ? "%" + keyword.trim() + "%" : null;
    }
}
