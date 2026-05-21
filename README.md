# Esun Financial System

## 專案簡介

本專案為一套金融商品喜好紀錄系統，採用前後端分離架構。後端以 Spring Boot + PostgreSQL 提供 RESTful API，前端以 Vue 3 + Vite 建置單頁應用，透過 Nginx 做反向代理與靜態資源伺服器，整合於 Docker Compose 一鍵啟動。

## 專案結構

```txt
.
├── backend/               Spring Boot Maven 後端專案
│   ├── src/main/java/     Java 原始碼（presentation / business / data / common）
│   ├── src/main/resources/ application.yml
│   └── Dockerfile
├── db/                    PostgreSQL 建表、初始化資料、存儲程式與索引
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
└── docker-compose.yml       三服務容器編排
```

## 系統架構

```txt
瀏覽器 → Nginx (:3000) ─┬─ /*      → Vue SPA 靜態檔案
                        └─ /api/* → proxy_pass → Spring Boot (:8080) → PostgreSQL (:5432)
```

### 技術棧

| 層級 | 技術 |
|------|------|
| 前端 | Vue 3、Vite、Axios |
| Web Server | Nginx 1.27（反向代理 + 靜態資源） |
| 後端 | Java 17、Spring Boot 3、Spring Web、Spring JDBC |
| 資料庫 | PostgreSQL 16、Stored Procedure |
| 建置工具 | Maven（後端）、npm（前端） |
| 容器化 | Docker Compose（postgres + backend + frontend） |

### 後端套件結構

```txt
backend/src/main/java/com/esun/financialsystem
├── presentation       REST API 控制器、Request/Response DTO
│   ├── controller
│   ├── request
│   └── response
├── business           商業邏輯服務、輸入驗證
│   └── service
├── data               JDBC repository、SQL 定義、RowMapper、資料模型
│   ├── mapper
│   ├── model
│   ├── repository
│   └── sql
└── common             共用例外處理、CORS 設定
    └── exception
```

### 前端功能

前端為單頁應用（SPA），透過 Tab 切換三個功能面板：

| Tab | 功能 | 對應 API |
|-----|------|----------|
| ❤️ 喜好商品 | 查詢 LikeList 清單（搜尋/排序/分頁）、新增/編輯/刪除收藏商品 | `/api/favorite-products/*` |
| 👤 使用者管理 | 使用者 CRUD、搜尋 | `/api/users/*` |
| 📦 商品管理 | 金融商品 CRUD、搜尋/排序 | `/api/products/*` |

## 資料庫設計

核心資料模型：

- `User` 1 對多 `LikeList`
- `Product` 1 對多 `LikeList`
- `User` 與 `Product` 透過 `LikeList` 建立多對多關係

`LikeList` 同時作為關聯表與收藏商品的商業資料儲存區，包含：

- `purchase_quantity`
- `account`
- `total_fee`
- `total_amount`

### PostgreSQL 連線設定

```txt
Host: localhost
Port: 5432
Database: esun_financial_system
User: esun_app_user
Password: EsunFinanceDB_2026!Secure
```

資料庫初始化時，`db/` 資料夾內的 SQL 檔案會掛載到容器的 `/docker-entrypoint-initdb.d/`，並於第一次建立資料卷時自動執行。

## 開發與啟動方式

### 方式一：Docker Compose 全套啟動（推薦）

一鍵啟動 PostgreSQL + Spring Boot + Nginx 前端：

```sh
docker compose up -d --build
```

啟動後存取：

| 服務 | 網址 |
|------|------|
| 前端（Nginx） | http://localhost:3000 |
| 後端 API（直接） | http://localhost:8081 |
| PostgreSQL | localhost:5432 |

### 方式二：本機開發模式

適合前端開發時使用，Vite Dev Server 提供 HMR 熱更新：

```sh
# 1. 啟動資料庫與後端
docker compose up -d postgres backend

# 2. 啟動前端 dev server（Vite proxy 自動轉發 /api → localhost:8081）
cd frontend
npm install
npm run dev
```

前端 dev server：http://localhost:5173

### 方式三：僅啟動部分服務

```sh
# 僅啟動 PostgreSQL
docker compose up -d postgres

# 本機直接啟動後端（需先啟動 PostgreSQL）
cd backend
mvn spring-boot:run
```

### 服務連線設定

| 項目 | 值 |
|------|----|
| Backend Port | 8081（Docker 對外）/ 8080（容器內） |
| Frontend Port | 3000（Nginx）/ 5173（Vite dev） |
| DB URL | jdbc:postgresql://localhost:5432/esun_financial_system |
| DB User | esun_app_user |
| DB Password | EsunFinanceDB_2026!Secure |

## API 快速導覽

後端預設 Base URL：

```txt
http://localhost:8081
```

### Favorite Product（收藏商品）API

- `GET /api/favorite-products/like-list`
- `POST /api/favorite-products`
- `GET /api/favorite-products/users/{userId}`
- `PUT /api/favorite-products/{sn}`
- `DELETE /api/favorite-products/{sn}`

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

### LikeList 查詢參數

```txt
userId
userName
email
account
keyword
page
pageSize
sortBy        => user_id | user_name | email | account
sortDirection => ASC | DESC
```

範例查詢：

```txt
GET /api/favorite-products/like-list?page=1&pageSize=10&keyword=王&sortBy=user_id&sortDirection=ASC
```

## Docker Compose 服務

| 服務 | 容器名稱 | 映像 | Port |
|------|----------|------|------|
| postgres | esun-financial-postgres | postgres:16.14-alpine | 5432 |
| backend | esun-financial-backend | 自建（Maven + JDK 17） | 8081→8080 |
| frontend | esun-financial-frontend | 自建（Node build → Nginx 1.27） | 3000→80 |

Nginx 設定（`frontend/nginx.conf`）：

- `/ ` → 提供 Vue SPA 靜態檔案，搭配 `try_files` fallback
- `/api/` → `proxy_pass http://backend:8080/api/`（反向代理）
- 啟用 Gzip 壓縮與靜態資源快取

## 安全性設計

### SQL Injection 防護

- 所有 JDBC 查詢使用 `JdbcTemplate` + `?` 參數化查詢（PreparedStatement）
- Favorite Product CUD 操作透過 PostgreSQL Stored Procedure，參數型別在函數簽名中定義
- `ORDER BY` 欄位使用白名單（`allowedColumns` Map），排序方向僅允許 `ASC` / `DESC`

### XSS 防護

- 前端使用 Vue 3 模板語法（`{{ }}`），預設自動 HTML 轉義
- 後端回應 JSON 格式，不直接輸出 HTML

#### `GET /api/users/{userId}`

Example:

```bash
curl "http://localhost:8081/api/users/A1236456789"
```

#### `POST /api/users`

Request body:

```json
{
  "userId": "C3234567891",
  "userName": "陳小美",
  "email": "chen@example.com",
  "account": "3333777888"
}
```

Example:

```bash
curl -X POST "http://localhost:8081/api/users" \
  -H "Content-Type: application/json" \
  -d '{"userId":"C3234567891","userName":"陳小美","email":"chen@example.com","account":"3333777888"}'
```

#### `PUT /api/users/{userId}`

Request body:

```json
{
  "userName": "陳小美-更新",
  "email": "chen.updated@example.com",
  "account": "3333777888"
}
```

Example:

```bash
curl -X PUT "http://localhost:8081/api/users/C3234567891" \
  -H "Content-Type: application/json" \
  -d '{"userName":"陳小美-更新","email":"chen.updated@example.com","account":"3333777888"}'
```

#### `DELETE /api/users/{userId}`

Example:

```bash
curl -X DELETE "http://localhost:8081/api/users/C3234567891"
```

### 2. Product CRUD

#### `GET /api/products`

Query parameters:

```txt
no
productName
keyword
priceMin
priceMax
feeRateMin
feeRateMax
page           default 1
pageSize       default 10, max 100
sortBy         => no | product_name | price | fee_rate | created_at | updated_at
sortDirection  => ASC | DESC
```

Example:

```bash
curl "http://localhost:8081/api/products?page=1&pageSize=10&keyword=基金&priceMin=10000&priceMax=20000&sortBy=no&sortDirection=ASC"
```

#### `GET /api/products/{no}`

Example:

```bash
curl "http://localhost:8081/api/products/1"
```

#### `POST /api/products`

Request body:

```json
{
  "productName": "亞洲平衡基金",
  "price": 22000,
  "feeRate": 0.011
}
```

Example:

```bash
curl -X POST "http://localhost:8081/api/products" \
  -H "Content-Type: application/json" \
  -d '{"productName":"亞洲平衡基金","price":22000,"feeRate":0.011}'
```

#### `PUT /api/products/{no}`

Request body:

```json
{
  "productName": "亞洲平衡基金-更新",
  "price": 22500,
  "feeRate": 0.012
}
```

Example:

```bash
curl -X PUT "http://localhost:8081/api/products/6" \
  -H "Content-Type: application/json" \
  -d '{"productName":"亞洲平衡基金-更新","price":22500,"feeRate":0.012}'
```

#### `DELETE /api/products/{no}`

Example:

```bash
curl -X DELETE "http://localhost:8081/api/products/6"
```

### 3. Favorite Product CRUD

#### `GET /api/favorite-products/like-list`

Query parameters:

```txt
userId
userName
email
account
keyword
page           default 1
pageSize       default 10, max 100
sortBy         => user_id | user_name | email | account
sortDirection  => ASC | DESC
```

Example:

```bash
curl "http://localhost:8081/api/favorite-products/like-list?page=1&pageSize=10&keyword=王&sortBy=user_id&sortDirection=ASC"
```

#### `POST /api/favorite-products`

Request body:

```json
{
  "userId": "A1236456789",
  "productNo": 1,
  "purchaseQuantity": 2,
  "account": "1111999666"
}
```

Example:

```bash
curl -X POST "http://localhost:8081/api/favorite-products" \
  -H "Content-Type: application/json" \
  -d '{"userId":"A1236456789","productNo":1,"purchaseQuantity":2,"account":"1111999666"}'
```

#### `GET /api/favorite-products/users/{userId}`

Example:

```bash
curl "http://localhost:8081/api/favorite-products/users/A1236456789"
```

#### `PUT /api/favorite-products/{sn}`

Request body:

```json
{
  "productNo": 2,
  "purchaseQuantity": 3,
  "account": "1111999666"
}
```

Example:

```bash
curl -X PUT "http://localhost:8081/api/favorite-products/1" \
  -H "Content-Type: application/json" \
  -d '{"productNo":2,"purchaseQuantity":3,"account":"1111999666"}'
```

#### `DELETE /api/favorite-products/{sn}`

Example:

```bash
curl -X DELETE "http://localhost:8081/api/favorite-products/1"
```
