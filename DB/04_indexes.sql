-- 04_indexes.sql
-- Common query indexes.

BEGIN;

DROP INDEX IF EXISTS idx_user_user_id_trgm;
DROP INDEX IF EXISTS idx_user_user_name_trgm;
DROP INDEX IF EXISTS idx_user_email_trgm;
DROP INDEX IF EXISTS idx_user_account_trgm;
DROP INDEX IF EXISTS idx_product_no_text_trgm;
DROP INDEX IF EXISTS idx_product_product_name_trgm;
DROP INDEX IF EXISTS idx_like_list_account_trgm;

DROP INDEX IF EXISTS idx_like_list_user_id;

CREATE INDEX IF NOT EXISTS idx_like_list_user_id_sn
    ON "LikeList" (user_id, sn);

CREATE INDEX IF NOT EXISTS idx_like_list_product_no
    ON "LikeList" (product_no);

CREATE INDEX IF NOT EXISTS idx_like_list_account
    ON "LikeList" (account);

CREATE INDEX IF NOT EXISTS idx_user_user_name
    ON "User" (user_name);

CREATE INDEX IF NOT EXISTS idx_user_email
    ON "User" (email);

CREATE INDEX IF NOT EXISTS idx_user_account
    ON "User" (account);

CREATE INDEX IF NOT EXISTS idx_user_created_at
    ON "User" (created_at);

CREATE INDEX IF NOT EXISTS idx_user_updated_at
    ON "User" (updated_at);

CREATE INDEX IF NOT EXISTS idx_product_product_name
    ON "Product" (product_name);

CREATE INDEX IF NOT EXISTS idx_product_price
    ON "Product" (price);

CREATE INDEX IF NOT EXISTS idx_product_fee_rate
    ON "Product" (fee_rate);

CREATE INDEX IF NOT EXISTS idx_product_created_at
    ON "Product" (created_at);

CREATE INDEX IF NOT EXISTS idx_product_updated_at
    ON "Product" (updated_at);

COMMIT;
