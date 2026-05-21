-- 03_procedures.sql
-- PostgreSQL functions for backend calls.

BEGIN;

DROP FUNCTION IF EXISTS sp_delete_favorite_product(BIGINT);
DROP FUNCTION IF EXISTS sp_update_favorite_product(BIGINT, BIGINT, INT, VARCHAR);
DROP FUNCTION IF EXISTS sp_count_like_list(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT);
DROP FUNCTION IF EXISTS sp_get_like_list(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT, VARCHAR, VARCHAR, INT, INT);
DROP FUNCTION IF EXISTS sp_get_favorite_products_by_user(VARCHAR);
DROP FUNCTION IF EXISTS sp_add_favorite_product(VARCHAR, BIGINT, INT, VARCHAR);

CREATE OR REPLACE FUNCTION sp_add_favorite_product(
    p_user_id VARCHAR(20),
    p_product_no BIGINT,
    p_purchase_quantity INT,
    p_account VARCHAR(30)
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_price NUMERIC;
    v_fee_rate NUMERIC;
    v_subtotal NUMERIC;
    v_total_fee NUMERIC(18, 2);
    v_total_amount NUMERIC(18, 2);
    v_sn BIGINT;
BEGIN
    IF p_purchase_quantity IS NULL OR p_purchase_quantity <= 0 THEN
        RAISE EXCEPTION 'purchase_quantity must be greater than 0'
            USING ERRCODE = '23514';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM "User" AS u
        WHERE u.user_id = p_user_id
    ) THEN
        RAISE EXCEPTION 'User % does not exist', p_user_id
            USING ERRCODE = '23503';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM "User" AS u
        WHERE u.user_id = p_user_id
          AND u.account = p_account
    ) THEN
        RAISE EXCEPTION 'Account % does not belong to user %', p_account, p_user_id
            USING ERRCODE = '23503';
    END IF;

    SELECT
        p.price,
        p.fee_rate
    INTO
        v_price,
        v_fee_rate
    FROM "Product" AS p
    WHERE p.no = p_product_no;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Product % does not exist', p_product_no
            USING ERRCODE = '23503';
    END IF;

    v_subtotal := v_price * p_purchase_quantity;
    v_total_fee := ROUND(v_subtotal * v_fee_rate, 2);
    v_total_amount := ROUND(v_subtotal + v_total_fee, 2);

    INSERT INTO "LikeList" (
        purchase_quantity,
        account,
        total_fee,
        total_amount,
        user_id,
        product_no
    ) VALUES (
        p_purchase_quantity,
        p_account,
        v_total_fee,
        v_total_amount,
        p_user_id,
        p_product_no
    )
    RETURNING sn INTO v_sn;

    RETURN v_sn;
END;
$$;

CREATE OR REPLACE FUNCTION sp_get_favorite_products_by_user(
    p_user_id VARCHAR(20)
)
RETURNS TABLE (
    sn BIGINT,
    user_id VARCHAR(20),
    user_name VARCHAR(100),
    email VARCHAR(255),
    product_no BIGINT,
    product_name VARCHAR(150),
    price NUMERIC(18, 2),
    fee_rate NUMERIC(8, 6),
    purchase_quantity INT,
    account VARCHAR(30),
    total_fee NUMERIC(18, 2),
    total_amount NUMERIC(18, 2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
    SELECT
        ll.sn,
        ll.user_id,
        u.user_name,
        u.email,
        ll.product_no,
        p.product_name,
        p.price,
        p.fee_rate,
        ll.purchase_quantity,
        ll.account,
        ll.total_fee,
        ll.total_amount,
        ll.created_at,
        ll.updated_at
    FROM "LikeList" AS ll
    INNER JOIN "User" AS u
        ON u.user_id = ll.user_id
    INNER JOIN "Product" AS p
        ON p.no = ll.product_no
    WHERE ll.user_id = p_user_id
    ORDER BY ll.sn;
END;
$$;

CREATE OR REPLACE FUNCTION sp_get_like_list(
    p_user_id VARCHAR(20),
    p_user_name VARCHAR(100),
    p_email VARCHAR(255),
    p_account VARCHAR(30),
    p_keyword TEXT,
    p_sort_by VARCHAR(30),
    p_sort_direction VARCHAR(4),
    p_page_size INT,
    p_offset INT
)
RETURNS TABLE (
    user_id VARCHAR(20),
    user_name VARCHAR(100),
    email VARCHAR(255),
    account VARCHAR(30),
    favorite_products TEXT
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.user_id,
        u.user_name,
        u.email,
        u.account,
        CAST(
            COALESCE(
                JSON_AGG(
                    JSON_BUILD_OBJECT(
                        'sn', ll.sn,
                        'productNo', p.no,
                        'productName', p.product_name,
                        'price', p.price,
                        'feeRate', p.fee_rate,
                        'purchaseQuantity', ll.purchase_quantity,
                        'account', ll.account,
                        'totalFee', ll.total_fee,
                        'totalAmount', ll.total_amount,
                        'createdAt', ll.created_at,
                        'updatedAt', ll.updated_at
                    )
                    ORDER BY ll.sn
                ),
                CAST('[]' AS json)
            ) AS TEXT
        ) AS favorite_products
    FROM "User" AS u
    INNER JOIN "LikeList" AS ll
        ON ll.user_id = u.user_id
    INNER JOIN "Product" AS p
        ON p.no = ll.product_no
    WHERE (p_user_id IS NULL OR u.user_id = p_user_id)
      AND (p_user_name IS NULL OR u.user_name ILIKE '%' || p_user_name || '%')
      AND (p_email IS NULL OR u.email ILIKE '%' || p_email || '%')
      AND (p_account IS NULL OR ll.account = p_account)
      AND (
          p_keyword IS NULL
          OR u.user_id ILIKE '%' || p_keyword || '%'
          OR u.user_name ILIKE '%' || p_keyword || '%'
          OR u.email ILIKE '%' || p_keyword || '%'
          OR ll.account ILIKE '%' || p_keyword || '%'
          OR p.product_name ILIKE '%' || p_keyword || '%'
      )
    GROUP BY
        u.user_id,
        u.user_name,
        u.email,
        u.account
    ORDER BY
        CASE WHEN COALESCE(p_sort_by, 'user_id') = 'user_id'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN u.user_id END ASC,
        CASE WHEN COALESCE(p_sort_by, 'user_id') = 'user_id'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN u.user_id END DESC,
        CASE WHEN p_sort_by = 'user_name'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN u.user_name END ASC,
        CASE WHEN p_sort_by = 'user_name'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN u.user_name END DESC,
        CASE WHEN p_sort_by = 'email'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN u.email END ASC,
        CASE WHEN p_sort_by = 'email'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN u.email END DESC,
        CASE WHEN p_sort_by = 'account'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN u.account END ASC,
        CASE WHEN p_sort_by = 'account'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN u.account END DESC,
        u.user_id ASC
    LIMIT COALESCE(NULLIF(p_page_size, 0), 10)
    OFFSET GREATEST(COALESCE(p_offset, 0), 0);
END;
$$;

CREATE OR REPLACE FUNCTION sp_count_like_list(
    p_user_id VARCHAR(20),
    p_user_name VARCHAR(100),
    p_email VARCHAR(255),
    p_account VARCHAR(30),
    p_keyword TEXT
)
RETURNS BIGINT
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_total BIGINT;
BEGIN
    SELECT COUNT(*)
    INTO v_total
    FROM (
        SELECT u.user_id
        FROM "User" AS u
        INNER JOIN "LikeList" AS ll
            ON ll.user_id = u.user_id
        INNER JOIN "Product" AS p
            ON p.no = ll.product_no
        WHERE (p_user_id IS NULL OR u.user_id = p_user_id)
          AND (p_user_name IS NULL OR u.user_name ILIKE '%' || p_user_name || '%')
          AND (p_email IS NULL OR u.email ILIKE '%' || p_email || '%')
          AND (p_account IS NULL OR ll.account = p_account)
          AND (
              p_keyword IS NULL
              OR u.user_id ILIKE '%' || p_keyword || '%'
              OR u.user_name ILIKE '%' || p_keyword || '%'
              OR u.email ILIKE '%' || p_keyword || '%'
              OR ll.account ILIKE '%' || p_keyword || '%'
              OR p.product_name ILIKE '%' || p_keyword || '%'
          )
        GROUP BY
            u.user_id,
            u.user_name,
            u.email,
            u.account
    ) AS like_list_count;

    RETURN COALESCE(v_total, 0);
END;
$$;

CREATE OR REPLACE FUNCTION sp_update_favorite_product(
    p_sn BIGINT,
    p_product_no BIGINT,
    p_purchase_quantity INT,
    p_account VARCHAR(30)
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_price NUMERIC;
    v_fee_rate NUMERIC;
    v_subtotal NUMERIC;
    v_total_fee NUMERIC(18, 2);
    v_total_amount NUMERIC(18, 2);
    v_updated_sn BIGINT;
    v_user_id VARCHAR(20);
BEGIN
    IF p_purchase_quantity IS NULL OR p_purchase_quantity <= 0 THEN
        RAISE EXCEPTION 'purchase_quantity must be greater than 0'
            USING ERRCODE = '23514';
    END IF;

    SELECT ll.user_id
    INTO v_user_id
    FROM "LikeList" AS ll
    WHERE ll.sn = p_sn;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'LikeList sn % does not exist', p_sn
            USING ERRCODE = 'P0002';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM "User" AS u
        WHERE u.user_id = v_user_id
          AND u.account = p_account
    ) THEN
        RAISE EXCEPTION 'Account % does not belong to user %', p_account, v_user_id
            USING ERRCODE = '23503';
    END IF;

    SELECT
        p.price,
        p.fee_rate
    INTO
        v_price,
        v_fee_rate
    FROM "Product" AS p
    WHERE p.no = p_product_no;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Product % does not exist', p_product_no
            USING ERRCODE = '23503';
    END IF;

    v_subtotal := v_price * p_purchase_quantity;
    v_total_fee := ROUND(v_subtotal * v_fee_rate, 2);
    v_total_amount := ROUND(v_subtotal + v_total_fee, 2);

    UPDATE "LikeList"
    SET
        purchase_quantity = p_purchase_quantity,
        account = p_account,
        total_fee = v_total_fee,
        total_amount = v_total_amount,
        product_no = p_product_no,
        updated_at = CURRENT_TIMESTAMP
    WHERE sn = p_sn
    RETURNING sn INTO v_updated_sn;

    RETURN v_updated_sn;
END;
$$;

CREATE OR REPLACE FUNCTION sp_delete_favorite_product(
    p_sn BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM "LikeList" AS ll
        WHERE ll.sn = p_sn
    ) THEN
        RAISE EXCEPTION 'LikeList sn % does not exist', p_sn
            USING ERRCODE = 'P0002';
    END IF;

    DELETE FROM "LikeList"
    WHERE sn = p_sn;

    RETURN TRUE;
END;
$$;

COMMIT;
