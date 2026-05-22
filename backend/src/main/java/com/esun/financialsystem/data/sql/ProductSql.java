package com.esun.financialsystem.data.sql;

public final class ProductSql {

    public static final String GET_PRODUCTS =
            "SELECT * FROM sp_get_products(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String COUNT_PRODUCTS =
            "SELECT sp_count_products(?, ?, ?, ?, ?, ?, ?)";

    public static final String GET_PRODUCT_BY_ID =
            "SELECT * FROM sp_get_product_by_id(?)";

    public static final String ADD_PRODUCT =
            "SELECT sp_add_product(?, ?, ?)";

    public static final String UPDATE_PRODUCT =
            "SELECT sp_update_product(?, ?, ?, ?)";

    public static final String DELETE_PRODUCT =
            "SELECT sp_delete_product(?)";

    private ProductSql() {
    }
}
