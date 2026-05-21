-- 03_procedures.sql
-- PostgreSQL functions for backend calls.

BEGIN;

DROP FUNCTION IF EXISTS sp_delete_favorite_product(BIGINT);
DROP FUNCTION IF EXISTS sp_update_favorite_product(BIGINT, BIGINT, INT, VARCHAR);
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
