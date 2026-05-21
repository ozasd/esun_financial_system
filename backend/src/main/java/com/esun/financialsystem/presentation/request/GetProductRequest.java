package com.esun.financialsystem.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public class GetProductRequest {

    private Long no;
    private String productName;
    private String keyword;

    @DecimalMin(value = "0.0", message = "priceMin must be greater than or equal to 0")
    private BigDecimal priceMin;

    @DecimalMin(value = "0.0", message = "priceMax must be greater than or equal to 0")
    private BigDecimal priceMax;

    @DecimalMin(value = "0.0", message = "feeRateMin must be greater than or equal to 0")
    private BigDecimal feeRateMin;

    @DecimalMin(value = "0.0", message = "feeRateMax must be greater than or equal to 0")
    private BigDecimal feeRateMax;

    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    private String sortBy = "no";
    private String sortDirection = "ASC";

    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public BigDecimal getPriceMin() {
        return priceMin;
    }

    public void setPriceMin(BigDecimal priceMin) {
        this.priceMin = priceMin;
    }

    public BigDecimal getPriceMax() {
        return priceMax;
    }

    public void setPriceMax(BigDecimal priceMax) {
        this.priceMax = priceMax;
    }

    public BigDecimal getFeeRateMin() {
        return feeRateMin;
    }

    public void setFeeRateMin(BigDecimal feeRateMin) {
        this.feeRateMin = feeRateMin;
    }

    public BigDecimal getFeeRateMax() {
        return feeRateMax;
    }

    public void setFeeRateMax(BigDecimal feeRateMax) {
        this.feeRateMax = feeRateMax;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
