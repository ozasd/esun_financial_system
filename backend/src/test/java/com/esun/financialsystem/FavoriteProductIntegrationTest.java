package com.esun.financialsystem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esun.financialsystem.presentation.request.PostFavoriteProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class FavoriteProductIntegrationTest extends PostgreSqlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getLikeListReturnsJsonAggregatedFavoriteProducts() throws Exception {
        mockMvc.perform(get("/api/favorite-products/like-list")
                        .param("keyword", "基金")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("sortBy", "user_id")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.datas[0].userId").value("A1236456789"))
                .andExpect(jsonPath("$.datas[0].favoriteProducts.length()").value(2))
                .andExpect(jsonPath("$.datas[0].favoriteProducts[0].productName").value("台股基金"));
    }

    @Test
    void postFavoriteProductCalculatesFeeAndCanBeFetchedByUser() throws Exception {
        PostFavoriteProductRequest request = new PostFavoriteProductRequest(
                "A1236456789",
                3L,
                2,
                "1111999666");

        mockMvc.perform(post("/api/favorite-products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sn").value(4))
                .andExpect(jsonPath("$.message").value("Favorite product created"));

        mockMvc.perform(get("/api/favorite-products/users/A1236456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[2].productNo").value(3))
                .andExpect(jsonPath("$[2].purchaseQuantity").value(2))
                .andExpect(jsonPath("$[2].totalFee").value(240.00))
                .andExpect(jsonPath("$[2].totalAmount").value(30240.00));
    }

    @Test
    void validationErrorsReturnBadRequestResponse() throws Exception {
        mockMvc.perform(get("/api/favorite-products/like-list")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/favorite-products/like-list"));
    }
}
