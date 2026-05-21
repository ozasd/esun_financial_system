package com.esun.financialsystem.repository;

import com.esun.financialsystem.dto.response.FavoriteProductResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcFavoriteProductRepository implements FavoriteProductRepository {

    private static final RowMapper<FavoriteProductResponse> FAVORITE_PRODUCT_ROW_MAPPER =
            JdbcFavoriteProductRepository::mapFavoriteProduct;

    private final JdbcTemplate jdbcTemplate;

    public JdbcFavoriteProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long addFavoriteProduct(String userId, long productNo, int purchaseQuantity, String account) {
        Long sn = jdbcTemplate.queryForObject(
                "SELECT sp_add_favorite_product(?, ?, ?, ?)",
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
                "SELECT * FROM sp_get_favorite_products_by_user(?)",
                FAVORITE_PRODUCT_ROW_MAPPER,
                userId);
    }

    @Override
    public long updateFavoriteProduct(long sn, long productNo, int purchaseQuantity, String account) {
        Long updatedSn = jdbcTemplate.queryForObject(
                "SELECT sp_update_favorite_product(?, ?, ?, ?)",
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
                "SELECT sp_delete_favorite_product(?)",
                Boolean.class,
                sn);
        return Boolean.TRUE.equals(deleted);
    }

    private static FavoriteProductResponse mapFavoriteProduct(ResultSet rs, int rowNum) throws SQLException {
        return new FavoriteProductResponse(
                rs.getLong("sn"),
                rs.getString("user_id"),
                rs.getString("user_name"),
                rs.getString("email"),
                rs.getLong("product_no"),
                rs.getString("product_name"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("fee_rate"),
                rs.getInt("purchase_quantity"),
                rs.getString("account"),
                rs.getBigDecimal("total_fee"),
                rs.getBigDecimal("total_amount"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
