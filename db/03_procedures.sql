-- 03_procedures.sql
-- PostgreSQL functions for backend calls.

BEGIN;

DROP FUNCTION IF EXISTS sp_delete_favorite_product(BIGINT);
DROP FUNCTION IF EXISTS sp_update_favorite_product(BIGINT, BIGINT, INT, VARCHAR);
DROP FUNCTION IF EXISTS sp_count_like_list(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT);
DROP FUNCTION IF EXISTS sp_get_like_list(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT, VARCHAR, VARCHAR, INT, INT);
DROP FUNCTION IF EXISTS sp_get_favorite_products_by_user(VARCHAR);
DROP FUNCTION IF EXISTS sp_add_favorite_product(VARCHAR, BIGINT, INT, VARCHAR);
DROP FUNCTION IF EXISTS sp_delete_product(BIGINT);
DROP FUNCTION IF EXISTS sp_update_product(BIGINT, VARCHAR, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS sp_add_product(VARCHAR, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS sp_get_product_by_id(BIGINT);
DROP FUNCTION IF EXISTS sp_count_products(BIGINT, VARCHAR, TEXT, NUMERIC, NUMERIC, NUMERIC, NUMERIC);
DROP FUNCTION IF EXISTS sp_get_products(BIGINT, VARCHAR, TEXT, NUMERIC, NUMERIC, NUMERIC, NUMERIC, VARCHAR, VARCHAR, INT, INT);
DROP FUNCTION IF EXISTS sp_delete_user(VARCHAR);
DROP FUNCTION IF EXISTS sp_update_user(VARCHAR, VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS sp_add_user(VARCHAR, VARCHAR, VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS sp_get_user_by_id(VARCHAR);
DROP FUNCTION IF EXISTS sp_count_users(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT);
DROP FUNCTION IF EXISTS sp_get_users(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT, VARCHAR, VARCHAR, INT, INT);

CREATE OR REPLACE FUNCTION sp_get_users(
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
    created_at TIMESTAMP,
    updated_at TIMESTAMP
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
        u.created_at,
        u.updated_at
    FROM "User" AS u
    WHERE (p_user_id IS NULL OR u.user_id = p_user_id)
      AND (p_user_name IS NULL OR u.user_name ILIKE '%' || p_user_name || '%')
      AND (p_email IS NULL OR u.email ILIKE '%' || p_email || '%')
      AND (p_account IS NULL OR u.account = p_account)
      AND (
          p_keyword IS NULL
          OR u.user_id ILIKE '%' || p_keyword || '%'
          OR u.user_name ILIKE '%' || p_keyword || '%'
          OR u.email ILIKE '%' || p_keyword || '%'
          OR u.account ILIKE '%' || p_keyword || '%'
      )
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
        CASE WHEN p_sort_by = 'created_at'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN u.created_at END ASC,
        CASE WHEN p_sort_by = 'created_at'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN u.created_at END DESC,
        CASE WHEN p_sort_by = 'updated_at'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN u.updated_at END ASC,
        CASE WHEN p_sort_by = 'updated_at'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN u.updated_at END DESC,
        u.user_id ASC
    LIMIT COALESCE(NULLIF(p_page_size, 0), 10)
    OFFSET GREATEST(COALESCE(p_offset, 0), 0);
END;
$$;

CREATE OR REPLACE FUNCTION sp_count_users(
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
    FROM "User" AS u
    WHERE (p_user_id IS NULL OR u.user_id = p_user_id)
      AND (p_user_name IS NULL OR u.user_name ILIKE '%' || p_user_name || '%')
      AND (p_email IS NULL OR u.email ILIKE '%' || p_email || '%')
      AND (p_account IS NULL OR u.account = p_account)
      AND (
          p_keyword IS NULL
          OR u.user_id ILIKE '%' || p_keyword || '%'
          OR u.user_name ILIKE '%' || p_keyword || '%'
          OR u.email ILIKE '%' || p_keyword || '%'
          OR u.account ILIKE '%' || p_keyword || '%'
      );

    RETURN COALESCE(v_total, 0);
END;
$$;

CREATE OR REPLACE FUNCTION sp_get_user_by_id(
    p_user_id VARCHAR(20)
)
RETURNS TABLE (
    user_id VARCHAR(20),
    user_name VARCHAR(100),
    email VARCHAR(255),
    account VARCHAR(30),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
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
        u.created_at,
        u.updated_at
    FROM "User" AS u
    WHERE u.user_id = p_user_id;
END;
$$;

CREATE OR REPLACE FUNCTION sp_add_user(
    p_user_id VARCHAR(20),
    p_user_name VARCHAR(100),
    p_email VARCHAR(255),
    p_account VARCHAR(30)
)
RETURNS VARCHAR(20)
LANGUAGE plpgsql
AS $$
DECLARE
    v_user_id VARCHAR(20);
BEGIN
    INSERT INTO "User" (
        user_id,
        user_name,
        email,
        account
    ) VALUES (
        p_user_id,
        p_user_name,
        p_email,
        p_account
    )
    RETURNING user_id INTO v_user_id;

    RETURN v_user_id;
END;
$$;

CREATE OR REPLACE FUNCTION sp_update_user(
    p_user_id VARCHAR(20),
    p_user_name VARCHAR(100),
    p_email VARCHAR(255),
    p_account VARCHAR(30)
)
RETURNS VARCHAR(20)
LANGUAGE plpgsql
AS $$
DECLARE
    v_user_id VARCHAR(20);
BEGIN
    UPDATE "User"
    SET
        user_name = p_user_name,
        email = p_email,
        account = p_account,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id
    RETURNING user_id INTO v_user_id;

    RETURN v_user_id;
END;
$$;

CREATE OR REPLACE FUNCTION sp_delete_user(
    p_user_id VARCHAR(20)
)
RETURNS VARCHAR(20)
LANGUAGE plpgsql
AS $$
DECLARE
    v_user_id VARCHAR(20);
BEGIN
    DELETE FROM "User"
    WHERE user_id = p_user_id
    RETURNING user_id INTO v_user_id;

    RETURN v_user_id;
END;
$$;

CREATE OR REPLACE FUNCTION sp_get_products(
    p_no BIGINT,
    p_product_name VARCHAR(150),
    p_keyword TEXT,
    p_price_min NUMERIC,
    p_price_max NUMERIC,
    p_fee_rate_min NUMERIC,
    p_fee_rate_max NUMERIC,
    p_sort_by VARCHAR(30),
    p_sort_direction VARCHAR(4),
    p_page_size INT,
    p_offset INT
)
RETURNS TABLE (
    no BIGINT,
    product_name VARCHAR(150),
    price NUMERIC(18, 2),
    fee_rate NUMERIC(8, 6),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.no,
        p.product_name,
        p.price,
        p.fee_rate,
        p.created_at,
        p.updated_at
    FROM "Product" AS p
    WHERE (p_no IS NULL OR p.no = p_no)
      AND (p_product_name IS NULL OR p.product_name ILIKE '%' || p_product_name || '%')
      AND (p_price_min IS NULL OR p.price >= p_price_min)
      AND (p_price_max IS NULL OR p.price <= p_price_max)
      AND (p_fee_rate_min IS NULL OR p.fee_rate >= p_fee_rate_min)
      AND (p_fee_rate_max IS NULL OR p.fee_rate <= p_fee_rate_max)
      AND (
          p_keyword IS NULL
          OR CAST(p.no AS TEXT) ILIKE '%' || p_keyword || '%'
          OR p.product_name ILIKE '%' || p_keyword || '%'
      )
    ORDER BY
        CASE WHEN COALESCE(p_sort_by, 'no') = 'no'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN p.no END ASC,
        CASE WHEN COALESCE(p_sort_by, 'no') = 'no'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN p.no END DESC,
        CASE WHEN p_sort_by = 'product_name'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN p.product_name END ASC,
        CASE WHEN p_sort_by = 'product_name'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN p.product_name END DESC,
        CASE WHEN p_sort_by = 'price'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN p.price END ASC,
        CASE WHEN p_sort_by = 'price'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN p.price END DESC,
        CASE WHEN p_sort_by = 'fee_rate'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN p.fee_rate END ASC,
        CASE WHEN p_sort_by = 'fee_rate'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN p.fee_rate END DESC,
        CASE WHEN p_sort_by = 'created_at'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN p.created_at END ASC,
        CASE WHEN p_sort_by = 'created_at'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN p.created_at END DESC,
        CASE WHEN p_sort_by = 'updated_at'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'ASC'
             THEN p.updated_at END ASC,
        CASE WHEN p_sort_by = 'updated_at'
              AND UPPER(COALESCE(p_sort_direction, 'ASC')) = 'DESC'
             THEN p.updated_at END DESC,
        p.no ASC
    LIMIT COALESCE(NULLIF(p_page_size, 0), 10)
    OFFSET GREATEST(COALESCE(p_offset, 0), 0);
END;
$$;

CREATE OR REPLACE FUNCTION sp_count_products(
    p_no BIGINT,
    p_product_name VARCHAR(150),
    p_keyword TEXT,
    p_price_min NUMERIC,
    p_price_max NUMERIC,
    p_fee_rate_min NUMERIC,
    p_fee_rate_max NUMERIC
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
    FROM "Product" AS p
    WHERE (p_no IS NULL OR p.no = p_no)
      AND (p_product_name IS NULL OR p.product_name ILIKE '%' || p_product_name || '%')
      AND (p_price_min IS NULL OR p.price >= p_price_min)
      AND (p_price_max IS NULL OR p.price <= p_price_max)
      AND (p_fee_rate_min IS NULL OR p.fee_rate >= p_fee_rate_min)
      AND (p_fee_rate_max IS NULL OR p.fee_rate <= p_fee_rate_max)
      AND (
          p_keyword IS NULL
          OR CAST(p.no AS TEXT) ILIKE '%' || p_keyword || '%'
          OR p.product_name ILIKE '%' || p_keyword || '%'
      );

    RETURN COALESCE(v_total, 0);
END;
$$;

CREATE OR REPLACE FUNCTION sp_get_product_by_id(
    p_no BIGINT
)
RETURNS TABLE (
    no BIGINT,
    product_name VARCHAR(150),
    price NUMERIC(18, 2),
    fee_rate NUMERIC(8, 6),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.no,
        p.product_name,
        p.price,
        p.fee_rate,
        p.created_at,
        p.updated_at
    FROM "Product" AS p
    WHERE p.no = p_no;
END;
$$;

CREATE OR REPLACE FUNCTION sp_add_product(
    p_product_name VARCHAR(150),
    p_price NUMERIC,
    p_fee_rate NUMERIC
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_no BIGINT;
BEGIN
    INSERT INTO "Product" (
        product_name,
        price,
        fee_rate
    ) VALUES (
        p_product_name,
        p_price,
        p_fee_rate
    )
    RETURNING no INTO v_no;

    RETURN v_no;
END;
$$;

CREATE OR REPLACE FUNCTION sp_update_product(
    p_no BIGINT,
    p_product_name VARCHAR(150),
    p_price NUMERIC,
    p_fee_rate NUMERIC
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_no BIGINT;
BEGIN
    UPDATE "Product"
    SET
        product_name = p_product_name,
        price = p_price,
        fee_rate = p_fee_rate,
        updated_at = CURRENT_TIMESTAMP
    WHERE no = p_no
    RETURNING no INTO v_no;

    RETURN v_no;
END;
$$;

CREATE OR REPLACE FUNCTION sp_delete_product(
    p_no BIGINT
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_no BIGINT;
BEGIN
    DELETE FROM "Product"
    WHERE no = p_no
    RETURNING no INTO v_no;

    RETURN v_no;
END;
$$;

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

    PERFORM 1
    FROM "User" AS u
    WHERE u.user_id = p_user_id
    FOR UPDATE;

    IF NOT FOUND THEN
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
    WHERE p.no = p_product_no
    FOR UPDATE;

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
    WHERE ll.sn = p_sn
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'LikeList sn % does not exist', p_sn
            USING ERRCODE = 'P0002';
    END IF;

    PERFORM 1
    FROM "User" AS u
    WHERE u.user_id = v_user_id
      AND u.account = p_account
    FOR UPDATE;

    IF NOT FOUND THEN
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
    WHERE p.no = p_product_no
    FOR UPDATE;

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
    PERFORM 1
    FROM "LikeList" AS ll
    WHERE ll.sn = p_sn
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'LikeList sn % does not exist', p_sn
            USING ERRCODE = 'P0002';
    END IF;

    DELETE FROM "LikeList"
    WHERE sn = p_sn;

    RETURN TRUE;
END;
$$;

COMMIT;
