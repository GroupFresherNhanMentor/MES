# Specification: Query Stock Balances Endpoint

## 1. Feature Description
This feature implements the `GET /api/stock-balances` HTTP REST endpoint in `InventoryController` to expose stock balance retrieval capabilities across all architectural layers. Users can query stock balances filtered optionally by `warehouseId` and `productId`.

## 2. User Scenarios & Testing

### 2.1 Scenario 1: Retrieve All Stock Balances for a Warehouse
- **Given:** Inventory records exist in the database for various warehouses and products
- **When:** An authenticated user issues a `GET /api/stock-balances?warehouseId={warehouseId}` request
- **Then:** The system returns a 200 OK response with a list of `StockBalanceDto` objects matching the specified warehouse.

### 2.2 Scenario 2: Retrieve Stock Balances Filtered by Product and Warehouse
- **Given:** Inventory records exist
- **When:** An authenticated user issues a `GET /api/stock-balances?warehouseId={warehouseId}&productId={productId}` request
- **Then:** The system returns a 200 OK response containing only balances for that specific warehouse and product.

### 2.3 Scenario 3: Unauthenticated Access
- **Given:** A request is sent without a valid authorization token
- **When:** A user issues a `GET /api/stock-balances` request
- **Then:** The system returns a 401 Unauthorized HTTP status.

## 3. Functional Requirements
- **FR-1:** Implement `GET /api/stock-balances` in `InventoryController` delegating to `InventoryUseCase.getStockBalances(warehouseId, productId)`.
- **FR-2:** Return `ResponseEntity<ApiResponse<List<StockBalanceDto>>>` wrapped in a standard `ApiResponse.success(data, "OK")`.
- **FR-3:** Support optional filtering by `warehouseId` and `productId` query parameters.
- **FR-4:** Ensure proper MapStruct DTO mapping from `StockBalance` entity to `StockBalanceDto`.
- **FR-5:** Add controller integration tests asserting successful 200 OK data retrieval and 401 Unauthorized handling.

## 4. Success Criteria
- **SC-1:** `GET /api/stock-balances` returns `200 OK` with non-empty balance lists for valid query params.
- **SC-2:** Calling `GET /api/stock-balances` no longer throws `UnsupportedOperationException`.
- **SC-3:** Integration tests in `InventoryIntegrationTest` cover the endpoint end-to-end.

## 5. Assumptions & Exclusions

### Assumptions
- The database tables and seed data for stock balances are already in place.

### Out of Scope
- Modifying the underlying database schema.
