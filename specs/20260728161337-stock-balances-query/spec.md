# Specification: Query Stock Balances Endpoint

## 1. Feature Description
This feature implements the stock balance query endpoint in `InventoryController` using `StockBalanceSearchRequest` (which encapsulates `page`, `size`, `warehouseId`, `productId`, `locationId`, `lotId`, and `stockStatusId`) as the input request DTO.

## 2. User Scenarios & Testing

### 2.1 Scenario 1: Retrieve All Stock Balances for a Warehouse
- **Given:** Inventory records exist in the database for various warehouses and products
- **When:** An authenticated user queries stock balances using a `StockBalanceSearchRequest` with `warehouseId` specified
- **Then:** The system returns a 200 OK response with a paginated `PageResponse<StockBalanceDto>` object containing balances matching the specified warehouse.

### 2.2 Scenario 2: Retrieve Stock Balances Filtered by Product and Warehouse
- **Given:** Inventory records exist
- **When:** An authenticated user queries stock balances using `StockBalanceSearchRequest` specifying both `warehouseId` and `productId`
- **Then:** The system returns a 200 OK response with a paginated `PageResponse<StockBalanceDto>` containing only balances for that specific warehouse and product.

### 2.3 Scenario 3: Unauthenticated Access
- **Given:** A request is sent without a valid authorization token
- **When:** A user issues a query request
- **Then:** The system returns a 401 Unauthorized HTTP status.

## 3. Functional Requirements
- **FR-1:** Update `InventoryController` endpoint to accept `StockBalanceSearchRequest request` object (containing `page`, `size`, `warehouseId`, `productId`, `locationId`, `lotId`, `stockStatusId`) instead of individual `@RequestParam` primitive parameters.
- **FR-2:** Return `ResponseEntity<ApiResponse<PageResponse<StockBalanceDto>>>` wrapped in a standard `ApiResponse.success(data, "OK")`.
- **FR-3:** Update `InventoryUseCase` and `InventoryService` to accept `StockBalanceSearchRequest request` and pass criteria to `balanceRepository.search(...)`.
- **FR-4:** Ensure proper MapStruct DTO mapping from `StockBalance` entity to `StockBalanceDto`.
- **FR-5:** Add controller integration tests asserting successful 200 OK paginated data retrieval and 401 Unauthorized handling.

## 4. Success Criteria
- **SC-1:** Stock balance query endpoint binds `StockBalanceSearchRequest` properly and returns `200 OK` with a valid `PageResponse<StockBalanceDto>`.
- **SC-2:** Calling the stock balance query endpoint no longer throws `UnsupportedOperationException`.
- **SC-3:** Integration tests in `InventoryIntegrationTest` cover the endpoint end-to-end.

## 5. Assumptions & Exclusions

### Assumptions
- `StockBalanceSearchRequest` extending `PageRequest` is already present in `fpt.qn.mes.inventory.application.dto.request`.

### Out of Scope
- Modifying underlying database schema or jOOQ tables.
