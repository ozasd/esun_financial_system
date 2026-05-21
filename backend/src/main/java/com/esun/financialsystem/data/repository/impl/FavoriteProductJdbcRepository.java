package com.esun.financialsystem.data.repository.impl;

import com.esun.financialsystem.data.mapper.FavoriteProductRowMapper;
import com.esun.financialsystem.data.mapper.LikeListRowMapper;
import com.esun.financialsystem.data.repository.FavoriteProductRepository;
import com.esun.financialsystem.data.sql.FavoriteProductSql;
import com.esun.financialsystem.data.sql.LikeListSql;
import com.esun.financialsystem.presentation.request.GetLikeListRequest;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class FavoriteProductJdbcRepository implements FavoriteProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final FavoriteProductRowMapper favoriteProductRowMapper;
    private final LikeListRowMapper likeListRowMapper;

    public FavoriteProductJdbcRepository(
            JdbcTemplate jdbcTemplate,
            FavoriteProductRowMapper favoriteProductRowMapper,
            LikeListRowMapper likeListRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.favoriteProductRowMapper = favoriteProductRowMapper;
        this.likeListRowMapper = likeListRowMapper;
    }

    @Override
    public long postFavoriteProduct(String userId, long productNo, int purchaseQuantity, String account) {
        Long sn = jdbcTemplate.queryForObject(
                FavoriteProductSql.ADD_FAVORITE_PRODUCT,
                Long.class,
                userId,
                productNo,
                purchaseQuantity,
                account);
        return sn == null ? 0L : sn;
    }

    @Override
    public List<FavoriteProductResponse> getFavoriteProductsByUser(String userId) {
        return jdbcTemplate.query(
                FavoriteProductSql.GET_FAVORITE_PRODUCTS_BY_USER,
                favoriteProductRowMapper,
                userId);
    }

    @Override
    public List<LikeListResponse> getLikeList(GetLikeListRequest request) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(request, parameters);
        String sql = LikeListSql.SELECT_PREFIX
                + whereClause
                + LikeListSql.GROUP_BY
                + buildOrderClause(request)
                + " LIMIT ? OFFSET ?";
        parameters.add(request.getPageSize() == null ? 10 : request.getPageSize());
        parameters.add(resolveOffset(request));
        return jdbcTemplate.query(sql, likeListRowMapper, parameters.toArray());
    }

    @Override
    public long countLikeList(GetLikeListRequest request) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(request, parameters);
        String sql = LikeListSql.COUNT_PREFIX
                + whereClause
                + LikeListSql.GROUP_BY
                + ") like_list_count";
        Long total = jdbcTemplate.queryForObject(sql, Long.class, parameters.toArray());
        return total == null ? 0L : total;
    }

    @Override
    public long putFavoriteProduct(long sn, long productNo, int purchaseQuantity, String account) {
        Long updatedSn = jdbcTemplate.queryForObject(
                FavoriteProductSql.UPDATE_FAVORITE_PRODUCT,
                Long.class,
                sn,
                productNo,
                purchaseQuantity,
                account);
        return updatedSn == null ? 0L : updatedSn;
    }

    @Override
    public boolean deleteFavoriteProduct(long sn) {
        Boolean deleted = jdbcTemplate.queryForObject(
                FavoriteProductSql.DELETE_FAVORITE_PRODUCT,
                Boolean.class,
                sn);
        return Boolean.TRUE.equals(deleted);
    }

    private String buildWhereClause(GetLikeListRequest request, List<Object> parameters) {
        StringBuilder whereClause = new StringBuilder(" WHERE TRUE ");
        String userId = trimToNull(request.getUserId());
        String userName = trimToNull(request.getUserName());
        String email = trimToNull(request.getEmail());
        String account = trimToNull(request.getAccount());
        String keyword = toKeywordPattern(request.getKeyword());

        if (userId != null) {
            whereClause.append(" AND u.user_id = ?");
            parameters.add(userId);
        }
        if (userName != null) {
            whereClause.append(" AND u.user_name ILIKE ?");
            parameters.add("%" + userName + "%");
        }
        if (email != null) {
            whereClause.append(" AND u.email ILIKE ?");
            parameters.add("%" + email + "%");
        }
        if (account != null) {
            whereClause.append(" AND l.account = ?");
            parameters.add(account);
        }
        if (keyword != null) {
            whereClause.append("""
                    AND (
                        u.user_id ILIKE ?
                        OR u.user_name ILIKE ?
                        OR u.email ILIKE ?
                        OR l.account ILIKE ?
                        OR p.product_name ILIKE ?
                    )
                    """);
            for (int i = 0; i < 5; i++) {
                parameters.add(keyword);
            }
        }

        return whereClause.toString();
    }

    private String buildOrderClause(GetLikeListRequest request) {
        Map<String, String> allowedColumns = new HashMap<>();
        allowedColumns.put("user_id", "u.user_id");
        allowedColumns.put("user_name", "u.user_name");
        allowedColumns.put("email", "u.email");
        allowedColumns.put("account", "u.account");

        String sortBy = trimToNull(request.getSortBy());
        String sortDirection = trimToNull(request.getSortDirection());

        String orderColumn = allowedColumns.getOrDefault(sortBy, "u.user_id");
        String direction = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";

        return " ORDER BY " + orderColumn + " " + direction;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String toKeywordPattern(String keyword) {
        return StringUtils.hasText(keyword) ? "%" + keyword.trim() + "%" : null;
    }

    private int resolveOffset(GetLikeListRequest request) {
        int page = request.getPage() == null ? 1 : request.getPage();
        int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
        return (page - 1) * pageSize;
    }
}
