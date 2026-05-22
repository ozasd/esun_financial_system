package com.esun.financialsystem.data.sql;

public final class UserSql {

    public static final String GET_USERS =
            "SELECT * FROM sp_get_users(?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String COUNT_USERS =
            "SELECT sp_count_users(?, ?, ?, ?, ?)";

    public static final String GET_USER_BY_ID =
            "SELECT * FROM sp_get_user_by_id(?)";

    public static final String ADD_USER =
            "SELECT sp_add_user(?, ?, ?, ?)";

    public static final String UPDATE_USER =
            "SELECT sp_update_user(?, ?, ?, ?)";

    public static final String DELETE_USER =
            "SELECT sp_delete_user(?)";

    private UserSql() {
    }
}
