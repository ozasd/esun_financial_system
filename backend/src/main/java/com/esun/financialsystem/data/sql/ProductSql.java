package com.esun.financialsystem.data.sql;

public final class ProductSql {

    public static final String SELECT_PREFIX = """
            SELECT
                p.no,
                p.product_name,
                p.price,
                p.fee_rate,
                p.created_at,
                p.updated_at
            FROM "Product" p
            """;

    public static final String COUNT_PREFIX = """
            SELECT COUNT(*)
            FROM "Product" p
            """;

    public static final String SELECT_BY_ID = SELECT_PREFIX + " WHERE p.no = ?";

    public static final String INSERT = """
            INSERT INTO "Product" (
                product_name,
                price,
                fee_rate
            ) VALUES (?, ?, ?)
            RETURNING no
            """;

    public static final String UPDATE = """
            UPDATE "Product"
            SET
                product_name = ?,
                price = ?,
                fee_rate = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE no = ?
            RETURNING no
            """;

    public static final String DELETE = """
            DELETE FROM "Product"
            WHERE no = ?
            RETURNING no
            """;

    private ProductSql() {
    }
}
