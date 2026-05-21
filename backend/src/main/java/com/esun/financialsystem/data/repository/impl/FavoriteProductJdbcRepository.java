package com.esun.financialsystem.data.repository.impl;

import com.esun.financialsystem.data.mapper.FavoriteProductRowMapper;
import com.esun.financialsystem.data.mapper.LikeListRowMapper;
import com.esun.financialsystem.data.repository.FavoriteProductRepository;
import com.esun.financialsystem.data.sql.FavoriteProductSql;
import com.esun.financialsystem.presentation.request.GetLikeListRequest;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import java.util.List;
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
        return jdbcTemplate.query(
                FavoriteProductSql.GET_LIKE_LIST,
                likeListRowMapper,
                trimToNull(request.getUserId()),
                trimToNull(request.getUserName()),
                trimToNull(request.getEmail()),
                trimToNull(request.getAccount()),
                trimToNull(request.getKeyword()),
                resolveSortBy(request),
                resolveSortDirection(request),
                resolvePageSize(request),
                resolveOffset(request));
    }

    @Override
    public long countLikeList(GetLikeListRequest request) {
        Long total = jdbcTemplate.queryForObject(
                FavoriteProductSql.COUNT_LIKE_LIST,
                Long.class,
                trimToNull(request.getUserId()),
                trimToNull(request.getUserName()),
                trimToNull(request.getEmail()),
                trimToNull(request.getAccount()),
                trimToNull(request.getKeyword()));
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

    @Override
    public String getUserIdBySn(long sn) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT user_id FROM \"LikeList\" WHERE sn = ?",
                    String.class,
                    sn);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String resolveSortBy(GetLikeListRequest request) {
        String sortBy = trimToNull(request.getSortBy());
        return sortBy == null ? "user_id" : sortBy;
    }

    private String resolveSortDirection(GetLikeListRequest request) {
        String sortDirection = trimToNull(request.getSortDirection());
        return "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
    }

    private int resolvePageSize(GetLikeListRequest request) {
        return request.getPageSize() == null ? 10 : request.getPageSize();
    }

    private int resolveOffset(GetLikeListRequest request) {
        int page = request.getPage() == null ? 1 : request.getPage();
        return (page - 1) * resolvePageSize(request);
    }
}
