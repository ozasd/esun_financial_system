package com.esun.financialsystem.data.sql;

public final class FavoriteProductSql {

    public static final String ADD_FAVORITE_PRODUCT =
            "SELECT sp_add_favorite_product(?, ?, ?, ?)";

    public static final String GET_FAVORITE_PRODUCTS_BY_USER =
            "SELECT * FROM sp_get_favorite_products_by_user(?)";

    public static final String UPDATE_FAVORITE_PRODUCT =
            "SELECT sp_update_favorite_product(?, ?, ?, ?)";

    public static final String DELETE_FAVORITE_PRODUCT =
            "SELECT sp_delete_favorite_product(?)";

    private FavoriteProductSql() {
    }
}
