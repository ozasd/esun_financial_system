package com.esun.financialsystem.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import com.esun.financialsystem.common.exception.BadRequestException;
import com.esun.financialsystem.data.repository.FavoriteProductRepository;
import com.esun.financialsystem.presentation.request.GetLikeListRequest;
import com.esun.financialsystem.presentation.request.PostFavoriteProductRequest;
import com.esun.financialsystem.presentation.response.LikeListResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class FavoriteProductServiceImplTest {

    @Mock
    private FavoriteProductRepository favoriteProductRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private FavoriteProductServiceImpl favoriteProductService;

    @Test
    void getLikeListUsesDefaultPaginationAndReturnsRepositoryData() {
        GetLikeListRequest request = new GetLikeListRequest();
        LikeListResponse response = new LikeListResponse(
                "A1236456789",
                "王小明",
                "test@email.com",
                "1111999666",
                List.of());

        when(favoriteProductRepository.getLikeList(request)).thenReturn(List.of(response));
        when(favoriteProductRepository.countLikeList(request)).thenReturn(1L);

        var result = favoriteProductService.getLikeList(request);

        assertThat(result.datas()).containsExactly(response);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(10);
    }

    @Test
    void getLikeListRejectsUnsupportedSortColumnBeforeRepositoryCall() {
        GetLikeListRequest request = new GetLikeListRequest();
        request.setSortBy("DROP TABLE User");

        assertThatThrownBy(() -> favoriteProductService.getLikeList(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("sortBy is not supported");
        verifyNoInteractions(favoriteProductRepository);
    }

    @Test
    void postFavoriteProductRejectsBlankUserId() {
        PostFavoriteProductRequest request = new PostFavoriteProductRequest(
                " ",
                1L,
                1,
                "1111999666");

        assertThatThrownBy(() -> favoriteProductService.postFavoriteProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("userId must not be blank");
        verifyNoInteractions(favoriteProductRepository);
    }

    @Test
    void postFavoriteProductDelegatesValidatedDataToRepository() {
        PostFavoriteProductRequest request = new PostFavoriteProductRequest(
                "A1236456789",
                1L,
                2,
                "1111999666");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("1"), any())).thenReturn(true);
        when(favoriteProductRepository.postFavoriteProduct("A1236456789", 1L, 2, "1111999666"))
                .thenReturn(9L);

        long sn = favoriteProductService.postFavoriteProduct(request);

        assertThat(sn).isEqualTo(9L);
        verify(favoriteProductRepository)
                .postFavoriteProduct("A1236456789", 1L, 2, "1111999666");
    }
}
