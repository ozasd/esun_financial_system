-- 04_indexes.sql
-- Common query indexes.

BEGIN;

CREATE INDEX IF NOT EXISTS idx_like_list_user_id
    ON "LikeList" (user_id);

CREATE INDEX IF NOT EXISTS idx_like_list_product_no
    ON "LikeList" (product_no);

CREATE INDEX IF NOT EXISTS idx_product_product_name
    ON "Product" (product_name);

CREATE INDEX IF NOT EXISTS idx_user_email
    ON "User" (email);

COMMIT;
