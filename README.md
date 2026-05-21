# Esun Financial System (高併發與系統設計優化範例)

## 專案簡介

本專案為一套金融商品喜好紀錄系統，除了提供基礎的 CRUD 功能外，更作為一個**系統設計 (System Design, SD) 的學習教材**。
專案採用前後端分離架構，並實作了多項應對高併發、資料一致性與高可用性的架構設計，包含 API Gateway 限流、Redis 快取與防雪崩機制、冪等性設計 (Idempotency) 以及 PostgreSQL 的行鎖與聚合查詢優化。

---

## 系統特色與架構亮點 (System Features)

本系統不僅僅是 Spring Boot + Vue 的簡單應用，更融合了以下業界常見的微服務與高併發架構設計：

1. **L7 負載均衡與 API 閘道器 (Kong API Gateway)**：所有外部請求先經過 Kong 進行限流 (Rate Limiting) 與 Round-robin 負載均衡，再轉發至後端多個實例。
2. **高併發讀取快取 (Redis Caching)**：使用 Redis 快取頻繁讀取的資料，大幅降低資料庫壓力。
3. **防止快取雪崩 (Cache Avalanche Prevention)**：採用「基礎時間 + 隨機時間」的 TTL 設計，避免大量快取在同一時間失效。
4. **資料庫層級的強一致性 (PostgreSQL 行鎖)**：透過 `SELECT ... FOR UPDATE` 避免 Race Condition，確保交易正確性。
5. **防止重複提交 (Idempotency 冪等性)**：利用 Redis 的 `SETNX` 機制，阻擋短時間內的重複操作。
6. **聚合查詢優化 (JSON_AGG)**：解決 ORM 常見的 N+1 Query 效能瓶頸。

---

## 系統設計 (SD) 優化解析教材

以下詳細說明本專案如何解決常見的系統設計問題，並標示了對應的程式碼位置供學習參考：

### 1. 應對高併發讀取：Redis 快取緩衝層
在高併發場景中，直接讀取資料庫會造成極大壓力。我們將使用者的最愛清單讀取加入 Redis 快取，作為資料庫前的緩衝層。當資料被更新或刪除時，我們會主動執行**快取失效 (Cache Eviction)**，以避免讀到舊資料。

**快取讀取流程圖：**
```mermaid
sequenceDiagram
    participant Client
    participant API Gateway
    participant Spring Boot
    participant Redis
    participant PostgreSQL

    Client->>API Gateway: GET /api/favorite-products/users/{id}
    API Gateway->>Spring Boot: 轉發請求
    Spring Boot->>Redis: 查詢 Cache (cache:user_favorites:{id})
    alt Cache Hit (命中)
        Redis-->>Spring Boot: 回傳 JSON 資料
    else Cache Miss (未命中)
        Redis-->>Spring Boot: 回傳 Null
        Spring Boot->>PostgreSQL: 查詢 DB
        PostgreSQL-->>Spring Boot: 回傳實體資料
        Spring Boot->>Redis: 將資料寫入 Cache (並加上 TTL)
    end
    Spring Boot-->>API Gateway: 回傳結果
    API Gateway-->>Client: 回傳 JSON 回應
```

### 2. 防止快取雪崩 (Cache Avalanche)
為了解決大量快取同時過期，導致請求瞬間湧入穿透到資料庫的問題，我們在設定快取時加入了隨機過期時間 (Jitter)，將快取失效的時間點打散。

* **程式碼標記**：
  * **寫入快取與隨機 TTL**：見 `backend/src/main/java/com/esun/financialsystem/business/service/impl/FavoriteProductServiceImpl.java` 內的 `getFavoriteProductsByUser` 方法。
    ```java
    // 基礎過期時間 5 分鐘 (300秒) + 隨機 0~300 秒，防止快取雪崩
    long ttlSeconds = 300 + ThreadLocalRandom.current().nextInt(301);
    redisTemplate.opsForValue().set(cacheKey, jsonData, ttlSeconds, TimeUnit.SECONDS);
    ```
  * **快取清除 (Eviction)**：見同檔案內的 `putFavoriteProduct` 與 `deleteFavoriteProduct` 方法呼叫的 `evictUserCache(userId)`。

### 3. 避免重複提交：Redis 冪等性 (Idempotency)
當使用者因為網路延遲連續點擊新增按鈕時，系統可能會寫入重複資料。我們利用 Redis 的 `setIfAbsent` (即 SETNX) 鎖住特定請求參數 10 秒鐘。如果同一個請求在 10 秒內重複出現，系統會直接拒絕。

* **程式碼標記**：
  * **Redis 冪等性檢查**：見 `FavoriteProductServiceImpl.java` 的 `postFavoriteProduct` 方法。
    ```java
    Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofSeconds(10));
    if (Boolean.FALSE.equals(success)) {
        throw new BadRequestException("Duplicate request, please try again later");
    }
    ```

### 4. 防止 Race Condition：DB 行鎖 (Pessimistic Lock)
在多個請求同時嘗試修改同一筆紀錄時，除了依賴前端或 Redis 的防護，我們在資料庫底層也加上了悲觀鎖 (Pessimistic Lock)。在 PostgreSQL 存儲過程中，利用 `FOR UPDATE` 鎖住關聯的 `User` 與 `Product` 資料列，確保在高併發下交易的強一致性。

* **程式碼標記**：
  * **PostgreSQL FOR UPDATE 行鎖**：見 `db/03_procedures.sql` 的 `sp_add_favorite_product` 函數。
    ```sql
    PERFORM 1 FROM "User" AS u WHERE u.user_id = p_user_id FOR UPDATE;
    ```

### 5. 資料庫查詢優化：解決 N+1 Query
查詢關聯資料時（例如查詢 User 與其下的所有 Favorite Products），若使用傳統做法會產生 1+N 次查詢。我們改為在 PostgreSQL 內直接使用 `JSON_AGG` 將多筆商品聚合成 JSON 字串回傳，大幅減少 DB 連線次數與傳輸成本。

* **程式碼標記與範例寫法**：
  * **JSON_AGG 聚合查詢**：見 `db/03_procedures.sql` 的 `sp_get_like_list` 函數。
    ```sql
    SELECT
        u.user_id,
        CAST(
            COALESCE(
                JSON_AGG(
                    JSON_BUILD_OBJECT(
                        'sn', ll.sn,
                        'productName', p.product_name,
                        'price', p.price
                    ) ORDER BY ll.sn
                ), '[]'::json
            ) AS TEXT
        ) AS favorite_products
    FROM "User" AS u
    INNER JOIN "LikeList" AS ll ON ll.user_id = u.user_id
    INNER JOIN "Product" AS p ON p.no = ll.product_no
    GROUP BY u.user_id;
    ```

### 6. 系統限流保護：API Gateway (Kong) Rate Limiting
為了防止惡意攻擊或單一使用者過度消耗系統資源，我們在 Kong API Gateway 層級加上了 Rate Limiting 插件，設定每個 IP 每分鐘最多只能請求 120 次。

* **程式碼標記與範例寫法**：
  * **限流設定**：見 `gateway/kong.yml` 中的 `plugins` 設定。
    ```yaml
    plugins:
      - name: rate-limiting
        config:
          minute: 120
          policy: local
    ```

---

## 專案結構

```txt
.
├── backend/               Spring Boot Maven 後端專案 (實作 Redis Cache, Idempotency)
│   ├── src/main/java/     Java 原始碼（presentation / business / data / common）
│   ├── src/main/resources/ application.yml
│   └── Dockerfile
├── db/                    PostgreSQL 建表、初始化資料、存儲程式與索引 (實作行鎖與聚合查詢)
│   ├── 01_schema.sql
│   ├── 02_seed.sql
│   ├── 03_procedures.sql
│   └── 04_indexes.sql
├── frontend/              Vue 3 + Vite 前端專案
│   ├── src/
│   │   ├── App.vue          主頁面（Tab 切換）
│   │   ├── api.js           Axios API 服務層
│   │   ├── style.css        全域設計系統
│   │   └── components/
│   │       ├── FavoritePanel.vue   喜好商品 CRUD
│   │       ├── UserPanel.vue       使用者管理 CRUD
│   │       └── ProductPanel.vue    商品管理 CRUD
│   ├── nginx.conf           Nginx 反向代理設定
│   └── Dockerfile           Multi-stage build（Node → Nginx）
├── gateway/               Kong DB-less 宣告式設定 (實作 Rate Limiting)
│   └── kong.yml
└── docker-compose.yml       多服務容器編排 (包含 Redis, Kong, DB, 雙 Backend 節點)
```

## 系統架構

```txt
瀏覽器 → Nginx (:3000) ─┬─ /*      → Vue SPA 靜態檔案
                        └─ /api/* → Kong (:8000) → Spring Boot backend x2 (:8080) ↔ Redis Cache (:6379)
                                                                                  ↘ PostgreSQL (:5432)
```

### 技術棧

| 層級 | 技術 |
|------|------|
| 前端 | Vue 3、Vite、Axios |
| Web Server | Nginx 1.27（反向代理 + 靜態資源） |
| API Gateway | Kong Gateway（DB-less mode、L7 Round-robin、Rate Limiting） |
| 後端 | Java 17、Spring Boot 3、Spring Data Redis、Spring Web、Spring JDBC |
| 資料庫 | PostgreSQL 16 (Stored Procedure)、Redis 7 |
| 容器化 | Docker Compose |

---

## 開發與啟動方式

### 方式一：Docker Compose 全套啟動（推薦）

一鍵啟動 PostgreSQL + Redis + 兩個 Spring Boot backend + Kong + Nginx 前端：

```sh
docker compose up -d --build
```

啟動後存取：

| 服務 | 網址 |
|------|------|
| 前端（Nginx） | http://localhost:3000 |
| Kong API Gateway | http://localhost:8000 |
| 後端 API（直接） | http://localhost:8081 |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |

### 方式二：本機開發模式

適合前端開發時使用，Vite Dev Server 提供 HMR 熱更新：

```sh
# 1. 啟動基礎設施
docker compose up -d postgres redis backend kong

# 2. 啟動前端 dev server（Vite proxy 自動轉發 /api → localhost:8000）
cd frontend
npm install
npm run dev
```

前端 dev server：http://localhost:5173

---

## API 快速導覽

API Gateway 預設 Base URL：`http://localhost:8000`

### Favorite Product（收藏商品）API
*(本區塊 API 已受 Redis 快取與冪等性防護)*

- `GET /api/favorite-products/like-list`
- `POST /api/favorite-products` *(具備 10 秒冪等性阻擋)*
- `GET /api/favorite-products/users/{userId}` *(具備 Redis 讀取快取與防雪崩)*
- `PUT /api/favorite-products/{sn}` *(更新後會主動 Evict 舊快取)*
- `DELETE /api/favorite-products/{sn}` *(刪除後會主動 Evict 舊快取)*

### User API

- `GET /api/users`
- `GET /api/users/{userId}`
- `POST /api/users`
- `PUT /api/users/{userId}`
- `DELETE /api/users/{userId}`

### Product API

- `GET /api/products`
- `GET /api/products/{no}`
- `POST /api/products`
- `PUT /api/products/{no}`
- `DELETE /api/products/{no}`

---
*此專案除了滿足基礎商業需求外，更適合作為進階系統設計的參考範例。歡迎查閱各模組原始碼，深入了解實作細節！*
