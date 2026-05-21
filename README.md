# Esun Financial System

## 專案簡介

本專案為一套以 Spring Boot + PostgreSQL 建置的金融收藏商品管理系統。後端提供 User、Product 與 Favorite Product（LikeList）三個核心資源的 CRUD API，並透過 Docker Compose 快速啟動後端與資料庫環境。

## 專案結構

```txt
.
├── backend/    Spring Boot Maven 後端專案
├── db/         PostgreSQL 建表、初始化資料、存儲程式與索引
├── frontend/   前端應用程式預留位置
└── docker-compose.yml
```

## 系統架構

後端主要採用以下技術：

- Java 17
- Spring Boot 3
- Maven
- Spring Web
- Spring JDBC
- PostgreSQL

### 主要套件與資料夾

```txt
backend/src/main/java/com/esun/financialsystem
├── presentation
│   ├── controller
│   ├── request
│   └── response
├── business
│   └── service
├── data
│   ├── mapper
│   ├── model
│   ├── repository
│   └── sql
└── common
    └── exception
```

- `presentation`：REST API 控制器、Request/Response DTO
- `business`：商業邏輯服務、輸入驗證
- `data`：JDBC repository、SQL 定義、RowMapper、資料模型
- `common`：共用例外處理

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

### 1. 啟動 PostgreSQL

```sh
docker compose up -d postgres
```

### 2. 同時啟動後端與資料庫

```sh
docker compose up -d
```

### 3. 重新建置後端映像檔

```sh
docker compose up -d --build
```

### 4. 直接從本機啟動後端

```sh
cd backend
mvn spring-boot:run
```

### 後端預設設定

```txt
Server Port: 8081
DB URL: jdbc:postgresql://localhost:5432/esun_financial_system
DB User: esun_app_user
DB Password: EsunFinanceDB_2026!Secure
```

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

## 目錄說明

- `backend/`：Spring Boot 後端程式碼與設定
- `db/`：資料庫 schema、測試資料、儲存程序與索引
- `frontend/`：前端應用程式預留位置
- `docker-compose.yml`：容器啟動與服務定義

## 備註

前端目前尚未實作，本專案重點為後端 API 與資料庫設計。若要新增前端，請於 `frontend/` 中建立對應專案結構。

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
