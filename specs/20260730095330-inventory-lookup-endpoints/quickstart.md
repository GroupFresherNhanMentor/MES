# Quickstart Guide: Inventory Reference Lookup Endpoints

## Verification Steps

### 1. Run Unit & Controller Tests
```bash
cd be && ./mvnw test -Djooq.codegen.skip=true -Dtest="InventoryControllerTest,InventoryServiceTest"
```

### 2. Verify HTTP Endpoints
- Call `GET /api/lot-types` $\to$ verify list of `LotTypeSummaryDto` objects returned with HTTP 200 OK.
- Call `GET /api/stock-statuses` $\to$ verify list of `StockStatusSummaryDto` objects returned with HTTP 200 OK.
- Call `GET /api/movement-types` $\to$ verify list of `MovementTypeSummaryDto` objects returned with HTTP 200 OK.
