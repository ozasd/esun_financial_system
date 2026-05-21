package com.esun.financialsystem.data.repository.impl;

import com.esun.financialsystem.data.mapper.FavoriteProductRowMapper;
import com.esun.financialsystem.data.repository.FavoriteProductRepository;
import com.esun.financialsystem.data.sql.FavoriteProductSql;
import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteProductJdbcRepository implements FavoriteProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final FavoriteProductRowMapper favoriteProductRowMapper;

    public FavoriteProductJdbcRepository(
            JdbcTemplate jdbcTemplate,
            FavoriteProductRowMapper favoriteProductRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.favoriteProductRowMapper = favoriteProductRowMapper;
    }

    @Override
    public long addFavoriteProduct(String userId, long productNo, int purchaseQuantity, String account) {
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
    public long updateFavoriteProduct(long sn, long productNo, int purchaseQuantity, String account) {
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
}
