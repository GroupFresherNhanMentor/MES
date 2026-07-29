# Quickstart Validation Guide: Stock-In API

## Verification Steps

### 1. Execute Unit & Controller Tests
Run Maven test suite to verify domain logic and endpoint mappings:
```bash
cd be && ./mvnw test -Dtest=InventoryServiceTest,StockLotSearchTest,StockBalanceSearchTest
```

### 2. Verify Stock-In Endpoint via cURL
Start backend and execute stock-in receipt:
```bash
curl -X POST http://localhost:8080/api/stock-in \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{
    "productId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "warehouseId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "locationId": "f1e2d3c4-b5a6-9788-7654-3210fedcba98",
    "lotNumber": "LOT-TEST-001",
    "quantity": 100.00,
    "referenceNo": "PO-1001"
  }'
```

### 3. Verify Stock Lots Filtering by Product ID
```bash
curl -X GET "http://localhost:8080/api/stock-lots?productId=9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d&page=0&size=10&sort=createdAt,desc" \
  -H "Authorization: Bearer <TOKEN>"
```

### 4. Verify Lot Ownership Rejection
Attempting stock-in with the same `LOT-TEST-001` but a different `productId` must be rejected with HTTP 400 Bad Request.
