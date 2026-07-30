# Feature Specification: Inventory Reference Lookup Endpoints

**Feature Branch**: `006-inventory-lookup-endpoints`
**Created**: 2026-07-30
**Status**: Draft
**Input**: User request: "help me code GET /lot-types /movement-types /stock-statuses, name to LotTypeSummaryDto also, don't use findAll(), change to search with criteria and request"

## Clarifications

### Session 2026-07-30
- Q: How should repository retrieval for lot types, stock statuses, and movement types be implemented? → A: Do not use `findAll()`. Implement `search(criteria)` and `count(criteria)` pattern using custom search criteria and search request DTOs (`LotTypeSearchCriteria`, `StockStatusSearchCriteria`, `MovementTypeSearchCriteria`).

## User Scenarios & Testing

### User Story 1 - Retrieve Lot Types Lookup List (Priority: P1)

As a warehouse operator or frontend developer, I want to fetch all available lot types (`GET /api/lot-types`) using criteria search returning `LotTypeSummaryDto` instances so that dropdown selectors can be populated when creating stock lots.

**Why this priority**: Required for creating stock lots with valid lot type reference IDs.

**Independent Test**: Calling `GET /api/lot-types` returns HTTP 200 OK with a list of `LotTypeSummaryDto` (`id`, `name`, `description`).

**Acceptance Scenarios**:
1. **Given** seeded or configured lot types in the database, **When** a user calls `GET /api/lot-types` with optional search criteria, **Then** the system returns HTTP 200 OK with `ApiResponse<List<LotTypeSummaryDto>>` containing matching lot types.

---

### User Story 2 - Retrieve Stock Statuses Lookup List (Priority: P1)

As a warehouse operator, I want to fetch all available stock statuses (`GET /api/stock-statuses`) using criteria search returning `StockStatusSummaryDto` instances so that inventory filter dropdowns and status selection fields show valid statuses (e.g., AVAILABLE, QUARANTINE, BLOCKED, EXPIRED).

**Why this priority**: Required for filtering stock balances and recording stock movements.

**Independent Test**: Calling `GET /api/stock-statuses` returns HTTP 200 OK with a list of `StockStatusSummaryDto` (`id`, `name`, `description`).

**Acceptance Scenarios**:
1. **Given** seeded or configured stock statuses, **When** a user calls `GET /api/stock-statuses` with optional search criteria, **Then** the system returns HTTP 200 OK with `ApiResponse<List<StockStatusSummaryDto>>` containing matching stock statuses.

---

### User Story 3 - Retrieve Movement Types Lookup List (Priority: P1)

As a warehouse operator, I want to fetch all available movement types (`GET /api/movement-types`) using criteria search returning `MovementTypeSummaryDto` instances so that inventory movement forms can display valid movement category options (e.g., PURCHASE_IN, ISSUE, TRANSFER, ADJUSTMENT).

**Why this priority**: Required for recording manual stock movements.

**Independent Test**: Calling `GET /api/movement-types` returns HTTP 200 OK with a list of `MovementTypeSummaryDto` (`id`, `name`, `description`).

**Acceptance Scenarios**:
1. **Given** seeded or configured movement types, **When** a user calls `GET /api/movement-types` with optional search criteria, **Then** the system returns HTTP 200 OK with `ApiResponse<List<MovementTypeSummaryDto>>` containing matching movement types.

---

## Requirements

### Functional Requirements

- **FR-001**: System MUST provide an unauthenticated public endpoint `GET /api/lot-types` accepting search request criteria and returning matching lot types as `ApiResponse<List<LotTypeSummaryDto>>`.
- **FR-002**: System MUST provide an unauthenticated public endpoint `GET /api/stock-statuses` accepting search request criteria and returning matching stock statuses as `ApiResponse<List<StockStatusSummaryDto>>`.
- **FR-003**: System MUST provide an unauthenticated public endpoint `GET /api/movement-types` accepting search request criteria and returning matching movement types as `ApiResponse<List<MovementTypeSummaryDto>>`.
- **FR-004**: System MUST define domain repositories (`LotTypeRepository`, `StockStatusRepository`, `MovementTypeRepository`) extending `BaseDomainRepository` using `search` & `count` with custom search criteria (`LotTypeSearchCriteria`, `StockStatusSearchCriteria`, `MovementTypeSearchCriteria`) instead of unpaginated `findAll()`.
- **FR-005**: All three endpoints MUST return HTTP 200 OK with `ApiResponse.success(data, "OK")`.

### Key Entities

- **LotType / LotTypeSummaryDto / LotTypeSearchCriteria**: Entity, summary DTO, and search criteria for lot types.
- **StockStatus / StockStatusSummaryDto / StockStatusSearchCriteria**: Entity, summary DTO, and search criteria for stock statuses.
- **MovementType / MovementTypeSummaryDto / MovementTypeSearchCriteria**: Entity, summary DTO, and search criteria for movement types.

## Success Criteria

- **SC-001**: `GET /api/lot-types`, `GET /api/stock-statuses`, and `GET /api/movement-types` return HTTP 200 OK with valid JSON lists of summary DTOs without throwing `UnsupportedOperationException`.
- **SC-002**: 100% of reference data repository access is executed via `search(criteria)` with jOOQ `DSLContext` queries without using `findAll()`.
