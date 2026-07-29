# Specification: Application Layer Paginated Search Response Refactoring

## 1. Feature Description
This feature standardizes application search responses to use `PageResponse<T>` at the UseCase and Service application ports, while maximizing utility reusability across all modules. It adds static helper methods to `PageResponse` to simplify building paginated responses from entity collections or mapper functions, and updates application interfaces (like `InventoryUseCase`) to return `PageResponse` for search/list methods (e.g. `getStockBalances`).

## 2. User Scenarios & Testing

### 2.1 Scenario 1: Reusable PageResponse Construction in Services
- **Given:** A service retrieves a list of items and total count from a repository
- **When:** The service constructs the `PageResponse<Dto>` using standard helper methods (e.g., `PageResponse.of(...)`)
- **Then:** Pagination fields (`totalElements`, `totalPages`, `pageNumber`, `pageSize`, `items`) are automatically computed consistently across all modules without boilerplate code.

### 2.2 Scenario 2: Paginated Stock Balance Query
- **Given:** An authenticated user queries stock balances via API
- **When:** The request includes page and size parameters (e.g., `?page=0&size=20`)
- **Then:** The Application layer returns `PageResponse<StockBalanceDto>` wrapped in an `ApiResponse`, providing complete pagination metadata.

## 3. Functional Requirements
- **FR-1:** Add static helper methods `PageResponse.of(List<T> items, long totalElements, int page, int size)` and `PageResponse.of(List<T> items, long totalElements, int page, int size, Function<T, U> mapper)` to `fpt.qn.mes.common.dto.response.PageResponse` to maximize reusability.
- **FR-2:** Refactor Application UseCases (such as `InventoryUseCase.getStockBalances`) to return `PageResponse<StockBalanceDto>` instead of raw `List<StockBalanceDto>`.
- **FR-3:** Update Application Services to use the common `PageResponse.of(...)` static factory method for constructing response envelopes.
- **FR-4:** Update Controllers to accept `page` and `size` parameters and return `ResponseEntity<ApiResponse<PageResponse<Dto>>>`.

## 4. Success Criteria
- **SC-1:** `PageResponse.of(...)` static helper methods are available and used in service implementations to eliminate duplicate pagination boilerplate.
- **SC-2:** All search/list methods in `InventoryUseCase` return `PageResponse<T>`.
- **SC-3:** All existing unit and integration tests compile and pass.

## 5. Assumptions & Exclusions

### Assumptions
- Repositories continue returning raw domain models or list collections, while pagination metadata calculation is standardized at the Application service layer.

### Out of Scope
- Altering database schema or jOOQ code generation.
