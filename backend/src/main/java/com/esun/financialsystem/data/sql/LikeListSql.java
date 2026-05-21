package com.esun.financialsystem.data.sql;

public final class LikeListSql {

    public static final String SELECT_PREFIX = """
            SELECT
                u.user_id,
                u.user_name,
                u.email,
                u.account,
                CAST(
                    COALESCE(
                        JSON_AGG(
                            JSON_BUILD_OBJECT(
                                'sn', l.sn,
                                'productNo', p.no,
                                'productName', p.product_name,
                                'price', p.price,
                                'feeRate', p.fee_rate,
                                'purchaseQuantity', l.purchase_quantity,
                                'account', l.account,
                                'totalFee', l.total_fee,
                                'totalAmount', l.total_amount,
                                'createdAt', l.created_at,
                                'updatedAt', l.updated_at
                            )
                        ),
                        CAST('[]' AS json)
                    ) AS text
                ) AS favorite_products
            FROM "User" u
            JOIN "LikeList" l ON l.user_id = u.user_id
            JOIN "Product" p ON p.no = l.product_no
            """;

    public static final String COUNT_PREFIX = """
            SELECT COUNT(*)
            FROM (
                SELECT u.user_id
                FROM "User" u
                JOIN "LikeList" l ON l.user_id = u.user_id
                JOIN "Product" p ON p.no = l.product_no
            """;

    public static final String GROUP_BY = """
            GROUP BY
                u.user_id,
                u.user_name,
                u.email,
                u.account
            """;

    private LikeListSql() {
    }
}
