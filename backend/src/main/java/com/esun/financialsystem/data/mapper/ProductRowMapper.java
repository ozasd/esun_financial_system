package com.esun.financialsystem.data.mapper;

import com.esun.financialsystem.presentation.response.ProductResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductRowMapper implements RowMapper<ProductResponse> {

    @Override
    public ProductResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProductResponse(
                rs.getLong("no"),
                rs.getString("product_name"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("fee_rate"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
