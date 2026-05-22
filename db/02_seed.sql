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
    ('B2234567890', '李小華', 'lee@example.com', '2222888999'),
    ('C3234567890', '陳怡君', 'chen@example.com', '3333777888'),
    ('D4234567890', '林志豪', 'lin@example.com', '4444666777'),
    ('E5234567890', '張雅婷', 'chang@example.com', '5555666000'),
    ('F6234567890', '黃俊傑', 'huang@example.com', '6666555444'),
    ('G7234567890', '吳佳蓉', 'wu@example.com', '7777444333'),
    ('H8234567890', '劉冠宇', 'liu@example.com', '8888333222'),
    ('I9234567890', '蔡佩珊', 'tsai@example.com', '9999222111'),
    ('J1034567890', '楊子萱', 'yang@example.com', '1000111222')
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
    (5, 'ESG 永續基金', 18000.00, 0.010000),
    (6, '亞太平衡基金', 16000.00, 0.009000),
    (7, '新興市場股票基金', 14000.00, 0.013000),
    (8, '美元貨幣市場基金', 9000.00, 0.004000),
    (9, '全球 REITs 基金', 22000.00, 0.011000),
    (10, '投資級公司債基金', 13000.00, 0.007000)
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
        (3, 'B2234567890', 3, 2, '2222888999'),
        (4, 'C3234567890', 4, 3, '3333777888'),
        (5, 'C3234567890', 6, 1, '3333777888'),
        (6, 'D4234567890', 5, 4, '4444666777'),
        (7, 'E5234567890', 7, 2, '5555666000'),
        (8, 'F6234567890', 8, 1, '6666555444'),
        (9, 'G7234567890', 9, 3, '7777444333'),
        (10, 'H8234567890', 10, 2, '8888333222'),
        (11, 'I9234567890', 1, 5, '9999222111'),
        (12, 'J1034567890', 2, 2, '1000111222'),
        (13, 'B2234567890', 4, 1, '2222888999'),
        (14, 'D4234567890', 6, 2, '4444666777'),
        (15, 'E5234567890', 3, 1, '5555666000'),
        (16, 'F6234567890', 5, 2, '6666555444'),
        (17, 'G7234567890', 7, 1, '7777444333'),
        (18, 'H8234567890', 8, 4, '8888333222')
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
