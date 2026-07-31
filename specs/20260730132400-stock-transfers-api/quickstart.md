# Quickstart & End-to-End Validation Guide: Stock Transfers API

## Overview
This guide describes how to run and validate the stock transfer endpoint (`POST /api/stock-transfers`).

## 1. Prerequisites
- Docker PostgreSQL container running (`docker-compose up -d`)
- Backend running (`cd be && ./mvnw spring-boot:run`) or unit/integration test suite

## 2. Unit Testing
Run unit tests verifying transfer logic, balance reduction/increase, movement logging, and validation edge cases:
```bash
cd be && ./mvnw test -Dtest=InventoryServiceTest#transferStock*
```

## 3. Integration Testing
Run slice and integration tests verifying HTTP request parsing, security, DB state changes, and transaction rollback on error:
```bash
cd be && ./mvnw test -Dtest=InventoryControllerTest#transferStock*,StockTransferIntegrationTest
```

## 4. Manual API Verification (cURL)

### Step 1: Obtain JWT Token
```bash
AUTH_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminpassword"}' | jq -r '.data.token')
```

### Step 2: Submit Stock Transfer Request
```bash
curl -i -X POST http://localhost:8080/api/stock-transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -d '{
    "fromWarehouseId": "<wh1_id>",
    "fromLocationId": "<a01_id>",
    "toWarehouseId": "<wh1_id>",
    "toLocationId": "<a02_id>",
    "productId": "<prod_id>",
    "lotId": "<lot_id>",
    "quantity": 20.00
  }'
```

### Step 3: Expected Outcome
- HTTP 200 OK response returning `StockTransferResponse`.
- Source location `fromLocationId` available balance is updated from 50 to 30.
- Destination location `toLocationId` available balance increases by 20.
- Two new stock movement records (`TRANSFER_OUT` and `TRANSFER_IN`) exist in `stock_movements`.
