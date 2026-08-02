# Research: Stock-In API & Stock Lot Management

## Decisions

### Decision 1: Stock Lot Product Ownership & Skip-If-Exists Strategy
- **Decision**: Look up existing `StockLot` by `lotNumber` via `lotRepository.findByLotNumber(lotNumber)`.
  - If existing lot is found:
    - Validate `existingLot.getProductId().equals(request.getProductId())`. If unequal, throw `AppException(ErrorCode.BAD_REQUEST, "Lot number belongs to a different product")`.
    - If equal, reuse `existingLot` and skip duplicate lot creation.
  - If no lot is found, create a new `StockLot`.
- **Rationale**: Prevents accidental reuse of lot numbers across different products while avoiding duplicate lot records for the same product during repeated stock receipts.
- **Alternatives Considered**: Allow duplicate lot numbers per product (rejected due to uniqueness expectation in inventory tracking).

### Decision 2: Endpoint Structure & Controller Integration
- **Decision**: Expose `POST /api/stock-in` in `InventoryController` (delegating to `InventoryUseCase.recordStockIn(StockInRequest request, UUID userId)`).
- **Rationale**: Keeps inventory operations under the standard Clean Architecture pattern in `fpt.qn.mes.inventory`.
- **Alternatives Considered**: Combine stock-in inside generic `recordMovement` endpoint (rejected for better REST API clarity and dedicated request validation).

### Decision 3: Paginated Stock Lots API
- **Decision**: Update `GET /api/stock-lots` to accept `@Valid StockLotSearchRequest request` and query `StockLotRepository.search(criteria)` & `StockLotRepository.count(criteria)`.
- **Rationale**: Matches standard `PageResponse<StockLotDto>` search pattern used across `StockBalance` and `StockMovement`.
