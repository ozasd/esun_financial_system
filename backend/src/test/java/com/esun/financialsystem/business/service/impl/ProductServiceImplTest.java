package com.esun.financialsystem.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.esun.financialsystem.common.exception.BadRequestException;
import com.esun.financialsystem.common.exception.ResourceNotFoundException;
import com.esun.financialsystem.data.repository.ProductRepository;
import com.esun.financialsystem.presentation.request.GetProductRequest;
import com.esun.financialsystem.presentation.request.PostProductRequest;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProductRejectsInvalidPriceRange() {
        GetProductRequest request = new GetProductRequest();
        request.setPriceMin(new BigDecimal("20000"));
        request.setPriceMax(new BigDecimal("10000"));

        assertThatThrownBy(() -> productService.getProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("priceMin must be less than or equal to priceMax");
        verifyNoInteractions(productRepository);
    }

    @Test
    void postProductTrimsNameAndDelegatesToRepository() {
        PostProductRequest request = new PostProductRequest(
                "  台股基金  ",
                new BigDecimal("10000.00"),
                new BigDecimal("0.010000"));

        when(productRepository.postProduct(
                "台股基金",
                new BigDecimal("10000.00"),
                new BigDecimal("0.010000")))
                .thenReturn(1L);

        long no = productService.postProduct(request);

        assertThat(no).isEqualTo(1L);
        verify(productRepository).postProduct(
                "台股基金",
                new BigDecimal("10000.00"),
                new BigDecimal("0.010000"));
    }

    @Test
    void deleteProductThrowsWhenRepositoryCannotDelete() {
        when(productRepository.deleteProduct(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product 99 does not exist");
    }

    @Test
    void getProductByIdRejectsNonPositiveNoBeforeRepositoryCall() {
        assertThatThrownBy(() -> productService.getProductById(0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("no must be greater than 0");
        verifyNoInteractions(productRepository);
    }

    @Test
    void getProductByIdThrowsWhenMissing() {
        when(productRepository.getProductById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product 99 does not exist");
    }
}
