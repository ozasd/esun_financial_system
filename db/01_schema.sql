-- 01_schema.sql
-- PostgreSQL schema for the financial product favorite system.
--
-- Relationship design:
-- 1. "User" to "LikeList" is 1:N.
-- 2. "Product" to "LikeList" is 1:N.
-- 3. Therefore "User" to "Product" is M:N through "LikeList".
-- 4. "LikeList" is a junction table with business payload
--    such as purchase_quantity, total_fee, and total_amount.

BEGIN;

DROP FUNCTION IF EXISTS sp_delete_favorite_product(BIGINT);
DROP FUNCTION IF EXISTS sp_update_favorite_product(BIGINT, BIGINT, INT, VARCHAR);
DROP FUNCTION IF EXISTS sp_count_like_list(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT);
DROP FUNCTION IF EXISTS sp_get_like_list(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT, VARCHAR, VARCHAR, INT, INT);
DROP FUNCTION IF EXISTS sp_get_favorite_products_by_user(VARCHAR);
DROP FUNCTION IF EXISTS sp_add_favorite_product(VARCHAR, BIGINT, INT, VARCHAR);

DROP TABLE IF EXISTS "LikeList";
DROP TABLE IF EXISTS "Like List";
DROP TABLE IF EXISTS favorite_products;
DROP TABLE IF EXISTS "Product";
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS "User";
DROP TABLE IF EXISTS users;

CREATE TABLE "User" (
    user_id VARCHAR(20) PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    account VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_account UNIQUE (account),
    CONSTRAINT uq_user_user_id_account UNIQUE (user_id, account)
);

CREATE TABLE "Product" (
    no BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(150) NOT NULL,
    price NUMERIC(18, 2) NOT NULL,
    fee_rate NUMERIC(8, 6) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_price_non_negative CHECK (price >= 0),
    CONSTRAINT chk_product_fee_rate_non_negative CHECK (fee_rate >= 0)
);

CREATE TABLE "LikeList" (
    sn BIGSERIAL PRIMARY KEY,
    purchase_quantity INT NOT NULL,
    account VARCHAR(30) NOT NULL,
    total_fee NUMERIC(18, 2) NOT NULL,
    total_amount NUMERIC(18, 2) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    product_no BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_like_list_user
        FOREIGN KEY (user_id)
        REFERENCES "User" (user_id),
    CONSTRAINT fk_like_list_user_account
        FOREIGN KEY (user_id, account)
        REFERENCES "User" (user_id, account),
    CONSTRAINT fk_like_list_product
        FOREIGN KEY (product_no)
        REFERENCES "Product" (no),
    CONSTRAINT chk_like_list_purchase_quantity_positive
        CHECK (purchase_quantity > 0),
    CONSTRAINT chk_like_list_total_fee_non_negative
        CHECK (total_fee >= 0),
    CONSTRAINT chk_like_list_total_amount_non_negative
        CHECK (total_amount >= 0)
);

COMMENT ON TABLE "User" IS
    'Master table for system users.';

COMMENT ON TABLE "Product" IS
    'Master table for financial products.';

COMMENT ON TABLE "LikeList" IS
    'Junction table for the many-to-many relationship between User and Product, with business payload.';

COMMIT;
