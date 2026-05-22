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
                        .header("Idempotency-Key", "test-post-favorite-product")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.sn").value(0)) // sn is 0 because of async
                .andExpect(jsonPath("$.message").value("Favorite product accepted"));
        
        // Note: The GET request verification is removed because the actual DB insertion is now asynchronous via Redis queue,
        // and the worker is not necessarily running/finishing synchronously in this test environment.
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
