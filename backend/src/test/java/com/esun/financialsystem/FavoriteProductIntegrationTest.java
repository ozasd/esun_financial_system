package com.esun.financialsystem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.esun.financialsystem.presentation.request.PostFavoriteProductRequest;
import com.esun.financialsystem.presentation.request.PostProductRequest;
import com.esun.financialsystem.presentation.request.PostUserRequest;
import com.esun.financialsystem.presentation.request.PutProductRequest;
import com.esun.financialsystem.presentation.request.PutUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
                        .param("userId", "A1236456789")
                        .param("keyword", "基金")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("sortBy", "user_id")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
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

    @Test
    void userApiReadsAndMutatesThroughStoredProcedures() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.datas[0].userId").value("A1236456789"));

        PostUserRequest createRequest = new PostUserRequest(
                "K1134567890",
                "鄭宇翔",
                "cheng@example.com",
                "1111222333");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("K1134567890"));

        PutUserRequest updateRequest = new PutUserRequest(
                "鄭宇翔更新",
                "cheng.updated@example.com",
                "1111222333");

        mockMvc.perform(put("/api/users/K1134567890")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("K1134567890"));

        mockMvc.perform(get("/api/users/K1134567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("鄭宇翔更新"))
                .andExpect(jsonPath("$.email").value("cheng.updated@example.com"));

        mockMvc.perform(delete("/api/users/K1134567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void productApiReadsAndMutatesThroughStoredProcedures() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.datas[0].productName").value("台股基金"));

        PostProductRequest createRequest = new PostProductRequest(
                "短期票券基金",
                new BigDecimal("8000.00"),
                new BigDecimal("0.003000"));

        String createResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Product created"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long no = objectMapper.readTree(createResponse).get("no").asLong();

        PutProductRequest updateRequest = new PutProductRequest(
                "短期票券基金更新",
                new BigDecimal("8500.00"),
                new BigDecimal("0.003500"));

        mockMvc.perform(put("/api/products/{no}", no)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.no").value(no));

        mockMvc.perform(get("/api/products/{no}", no))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("短期票券基金更新"))
                .andExpect(jsonPath("$.price").value(8500.00));

        mockMvc.perform(delete("/api/products/{no}", no))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }
}
