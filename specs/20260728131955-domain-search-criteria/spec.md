# Feature Specification: Inventory Domain Search Criteria & Repository PageResponse Refactoring

**Feature Branch**: `003-domain-search-criteria`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "in repository PaginationResult switch to PageResponse, continue with previous specs"

## Clarifications

### Session 2026-07-28
- Q: What return type should repository paginated query methods use? → A: Use PageResponse<T> instead of PaginationResult<T> across repository interfaces (StockBalanceRepository, StockLotRepository, StockMovementRepository) and persistence adapters.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Paginated Search for StockBalances returning PageResponse (Priority: P1)

As an inventory application service developer, I want `StockBalanceSearchCriteria` extending `PageRequest` and repository methods returning `PageResponse<StockBalance>` so that stock balance search queries return complete pagination metadata (`items`, `totalElements`, `totalPages`, `pageNumber`, `pageSize`) directly.

**Why this priority**: Enables flexible, paginated stock balance searches with standardized `PageResponse` output.

**Independent Test**: Can be tested via repository persistence adapter unit/integration tests by passing `StockBalanceSearchCriteria` with filtering fields and page settings, then verifying the returned `PageResponse<StockBalance>` contains matching items, total elements, and calculated total pages.

**Acceptance Scenarios**:

1. **Given** a `StockBalanceSearchCriteria` with `warehouseId` and `productId`, **When** querying `StockBalanceRepository.findByCriteria(criteria)`, **Then** a `PageResponse<StockBalance>` containing filtered items and metadata is returned.
2. **Given** a `StockBalanceSearchCriteria` with default `PageRequest` settings (`page = 0`, `size = 20`), **When** querying without filters, **Then** up to 20 records are returned inside `PageResponse<StockBalance>`.

---

### User Story 2 - Paginated Search for StockLots returning PageResponse (Priority: P2)

As an inventory application service developer, I want `StockLotSearchCriteria` extending `PageRequest` and repository methods returning `PageResponse<StockLot>` so that lot queries provide complete pagination metadata.

**Why this priority**: Supports batch and lot traceability lookups with unified pagination metadata.

**Independent Test**: Tested via `StockLotRepository` tests by executing paginated lot searches with lot number filters.

**Acceptance Scenarios**:

1. **Given** a `StockLotSearchCriteria` with a `lotNumber` filter and page parameters, **When** executing search, **Then** matching lots are returned inside `PageResponse<StockLot>`.

---

### User Story 3 - Paginated Search for StockMovements returning PageResponse (Priority: P3)

As an inventory application service developer, I want `StockMovementSearchCriteria` extending `PageRequest` and repository methods returning `PageResponse<StockMovement>` so that movement ledger logs return standardized `PageResponse`.

**Why this priority**: Essential for movement audit ledgers and transaction history searches.

**Independent Test**: Tested via `StockMovementRepository` search queries.

**Acceptance Scenarios**:

1. **Given** a `StockMovementSearchCriteria` with `referenceNo` and `movementTypeId`, **When** querying movements, **Then** matching transaction logs are returned in a `PageResponse<StockMovement>`.

---

### Edge Cases

- **Null Filter Parameters**: Passing null filter fields in criteria objects results in unconstrained matching for those specific fields.
- **Default Page Boundaries**: If `page` or `size` are not explicitly set, `PageRequest` defaults (`page = 0`, `size = 20`) apply when building `PageResponse`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide domain-level search criteria classes (`StockBalanceSearchCriteria`, `StockLotSearchCriteria`, `StockMovementSearchCriteria`) extending `fpt.qn.mes.common.dto.request.PageRequest`.
- **FR-002**: Search criteria classes MUST inherit `page` and `size` fields from `PageRequest`.
- **FR-003**: `StockBalanceSearchCriteria` MUST support optional filters: `warehouseId`, `locationId`, `productId`, `lotId`, `stockStatusId`.
- **FR-004**: `StockLotSearchCriteria` MUST support optional filters: `productId`, `lotTypeId`, `lotNumber`, `expiryBefore`.
- **FR-005**: `StockMovementSearchCriteria` MUST support optional filters: `movementTypeId`, `productId`, `lotId`, `warehouseId`, `locationId`, `referenceNo`.
- **FR-006**: Repository interfaces (`StockBalanceRepository`, `StockLotRepository`, `StockMovementRepository`) MUST return `PageResponse<T>` (from `fpt.qn.mes.common.dto.response.PageResponse`) instead of `PaginationResult<T>`.
- **FR-007**: Persistence adapters MUST construct dynamic jOOQ `where` conditions based on non-null fields in search criteria and calculate `totalPages`, `totalElements`, `pageNumber`, and `pageSize` inside `PageResponse<T>`.

### Key Entities *(include if feature involves data)*

- **PageRequest**: Base class in `fpt.qn.mes.common.dto.request` holding `int page` and `int size`.
- **PageResponse**: Pagination response wrapper in `fpt.qn.mes.common.dto.response`.
- **StockBalanceSearchCriteria**: Extends `PageRequest` with stock balance search filters.
- **StockLotSearchCriteria**: Extends `PageRequest` with stock lot search filters.
- **StockMovementSearchCriteria**: Extends `PageRequest` with stock movement search filters.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of inventory repository paginated methods return `PageResponse<T>` instead of `PaginationResult<T>`.
- **SC-002**: 100% of search criteria classes extend `fpt.qn.mes.common.dto.request.PageRequest`.
- **SC-003**: All unit and integration test suites compile and pass cleanly with `PageResponse<T>`.

## Assumptions

- `PageResponse` in `fpt.qn.mes.common.dto.response` serves as the standard pagination response wrapper across repository interfaces and services.
