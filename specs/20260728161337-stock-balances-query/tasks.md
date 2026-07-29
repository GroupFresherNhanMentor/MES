# Implementation Tasks: Query Stock Balances Endpoint

## Phase 1: Setup & Port Signature Updates
- [X] T001 Update `InventoryUseCase.java` interface to declare `PageResponse<StockBalanceDto> getStockBalances(StockBalanceSearchRequest request)` in `be/src/main/java/fpt/qn/mes/inventory/application/port/in/InventoryUseCase.java`

## Phase 2: Service Layer Implementation
- [X] T002 Implement `getStockBalances(StockBalanceSearchRequest request)` in `InventoryService.java` mapping `StockBalanceSearchRequest` to `StockBalanceSearchCriteria`, calling `balanceRepository.search(criteria)`, and returning `PageResponse<StockBalanceDto>` using `PageResponse.of(...)` in `be/src/main/java/fpt/qn/mes/inventory/application/service/InventoryService.java`

## Phase 3: Controller Layer Implementation
- [X] T003 Update `getStockBalances` endpoint in `InventoryController.java` to accept `@Valid StockBalanceSearchRequest request` and return `ResponseEntity<ApiResponse<PageResponse<StockBalanceDto>>>` in `be/src/main/java/fpt/qn/mes/inventory/presentation/InventoryController.java`

## Phase 4: Unit & Integration Testing
- [X] T004 Update `InventoryServiceTest.java` unit test to mock `getStockBalances(StockBalanceSearchRequest)` and verify `PageResponse` output in `be/src/test/java/fpt/qn/mes/inventory/service/InventoryServiceTest.java`
- [X] T005 [P] Add integration test in `InventoryIntegrationTest.java` verifying `GET /api/stock-balances` endpoint returns `200 OK` with `PageResponse` in `be/src/test/java/fpt/qn/mes/inventory/integration/InventoryIntegrationTest.java`

## Phase 5: Verification & Polish
- [X] T006 Run `./mvnw clean test` to ensure all tests pass and `GET /api/stock-balances` functions cleanly end-to-end.
