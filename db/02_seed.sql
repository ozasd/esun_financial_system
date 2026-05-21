-- 02_seed.sql
-- Repeatable test data for local development and interview review.

BEGIN;

INSERT INTO "User" (
    user_id,
    user_name,
    email,
    account
) VALUES
    ('A1236456789', '王小明', 'test@email.com', '1111999666'),
    ('B2234567890', '李小華', 'lee@example.com', '2222888999')
ON CONFLICT (user_id) DO UPDATE SET
    user_name = EXCLUDED.user_name,
    email = EXCLUDED.email,
    account = EXCLUDED.account,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO "Product" (
    no,
    product_name,
    price,
    fee_rate
) VALUES
    (1, '台股基金', 10000.00, 0.010000),
    (2, '美國科技基金', 20000.00, 0.015000),
    (3, '全球債券基金', 15000.00, 0.008000),
    (4, '高收益債基金', 12000.00, 0.012000),
    (5, 'ESG 永續基金', 18000.00, 0.010000)
ON CONFLICT (no) DO UPDATE SET
    product_name = EXCLUDED.product_name,
    price = EXCLUDED.price,
    fee_rate = EXCLUDED.fee_rate,
    updated_at = CURRENT_TIMESTAMP;

WITH favorite_seed (
    sn,
    user_id,
    product_no,
    purchase_quantity,
    account
) AS (
    VALUES
        (1, 'A1236456789', 1, 2, '1111999666'),
        (2, 'A1236456789', 2, 1, '1111999666'),
        (3, 'B2234567890', 3, 2, '2222888999')
),
calculated_favorites AS (
    SELECT
        fs.sn,
        fs.user_id,
        fs.product_no,
        fs.purchase_quantity,
        fs.account,
        ROUND(p.price * fs.purchase_quantity * p.fee_rate, 2) AS total_fee,
        ROUND(
            (p.price * fs.purchase_quantity)
            + ROUND(p.price * fs.purchase_quantity * p.fee_rate, 2),
            2
        ) AS total_amount
    FROM favorite_seed AS fs
    INNER JOIN "Product" AS p
        ON p.no = fs.product_no
)
INSERT INTO "LikeList" (
    sn,
    purchase_quantity,
    account,
    total_fee,
    total_amount,
    user_id,
    product_no
)
SELECT
    sn,
    purchase_quantity,
    account,
    total_fee,
    total_amount,
    user_id,
    product_no
FROM calculated_favorites
ON CONFLICT (sn) DO UPDATE SET
    purchase_quantity = EXCLUDED.purchase_quantity,
    account = EXCLUDED.account,
    total_fee = EXCLUDED.total_fee,
    total_amount = EXCLUDED.total_amount,
    user_id = EXCLUDED.user_id,
    product_no = EXCLUDED.product_no,
    updated_at = CURRENT_TIMESTAMP;

SELECT setval(
    pg_get_serial_sequence('"Product"', 'no'),
    (SELECT MAX(no) FROM "Product"),
    TRUE
);

SELECT setval(
    pg_get_serial_sequence('"LikeList"', 'sn'),
    (SELECT MAX(sn) FROM "LikeList"),
    TRUE
);

COMMIT;
