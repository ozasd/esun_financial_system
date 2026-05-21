package com.esun.financialsystem.data.mapper;

import com.esun.financialsystem.presentation.response.FavoriteProductResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class FavoriteProductRowMapper implements RowMapper<FavoriteProductResponse> {

    @Override
    public FavoriteProductResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
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
