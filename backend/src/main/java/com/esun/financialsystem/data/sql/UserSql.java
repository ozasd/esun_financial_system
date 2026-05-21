package com.esun.financialsystem.data.sql;

public final class UserSql {

    public static final String SELECT_PREFIX = """
            SELECT
                u.user_id,
                u.user_name,
                u.email,
                u.account,
                u.created_at,
                u.updated_at
            FROM "User" u
            """;

    public static final String COUNT_PREFIX = """
            SELECT COUNT(*)
            FROM "User" u
            """;

    public static final String SELECT_BY_ID = SELECT_PREFIX + " WHERE u.user_id = ?";

    public static final String INSERT = """
            INSERT INTO "User" (
                user_id,
                user_name,
                email,
                account
            ) VALUES (?, ?, ?, ?)
            RETURNING user_id
            """;

    public static final String UPDATE = """
            UPDATE "User"
            SET
                user_name = ?,
                email = ?,
                account = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = ?
            RETURNING user_id
            """;

    public static final String DELETE = """
            DELETE FROM "User"
            WHERE user_id = ?
            RETURNING user_id
            """;

    private UserSql() {
    }
}
