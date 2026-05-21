package com.esun.financialsystem.data.mapper;

import com.esun.financialsystem.presentation.response.FavoriteProductSummaryResponse;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class LikeListRowMapper implements RowMapper<LikeListResponse> {

    private static final TypeReference<List<FavoriteProductSummaryResponse>> FAVORITE_PRODUCT_LIST_TYPE =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;

    public LikeListRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LikeListResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LikeListResponse(
                rs.getString("user_id"),
                rs.getString("user_name"),
                rs.getString("email"),
                rs.getString("account"),
                parseFavoriteProducts(rs.getString("favorite_products")));
    }

    private List<FavoriteProductSummaryResponse> parseFavoriteProducts(String favoriteProducts) throws SQLException {
        try {
            return objectMapper.readValue(favoriteProducts, FAVORITE_PRODUCT_LIST_TYPE);
        } catch (IOException ex) {
            throw new SQLException("Failed to parse favorite_products JSON", ex);
        }
    }
}
