# Feature Specification: Get BOM List and BOM Detail (Query BOMs)

**Feature Branch**: `feature/bom-004-query-boms`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "Get BOM List (with pagination and product/status filtering) and Get BOM Detail (with full line item breakdown)"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Get Paginated BOM List with Optional Filters (Priority: P1)

As a Production Planner or Operator, I want to query a paginated list of BOMs with optional filters for `finishedProductId` and `bomStatusId` so that I can easily find relevant production formulas and recipes.

**Why this priority**: Core navigation capability required across all MES roles (Planner, Operator, QC Inspector, Manager) to view available BOM specifications.

**Independent Test**: Send `GET /api/boms?page=0&size=10&finishedProductId=<UUID>&bomStatusId=<UUID>` and verify paginated result containing matching BOM headers.

**Acceptance Scenarios**:

1. **Given** 15 BOMs exist in the system, **When** a user requests `GET /api/boms?page=0&size=10`, **Then** the system returns page 0 with 10 BOM items, `totalElements = 15`, and `totalPages = 2`.
2. **Given** multiple BOMs for various products, **When** a user filters by `finishedProductId`, **Then** only BOMs belonging to that specific product are returned.
3. **Given** BOMs in various statuses (`DRAFT`, `ACTIVE`, `INACTIVE`), **When** a user filters by `bomStatusId` (e.g. `ACTIVE`), **Then** only active BOMs are returned.

---

### User Story 2 - Get Full BOM Detail by ID (Priority: P1)

As a Production Planner or Quality Inspector, I want to retrieve full detail for a specific BOM by its ID including all component line items (`bom_items`) so that I can inspect exact component recipe quantities and scrap rates.

**Why this priority**: Required to inspect full production specifications before starting Work Orders or conducting quality verification.

**Independent Test**: Send `GET /api/boms/{id}` for an existing BOM ID and verify that the response returns complete header metadata plus the list of component items with material details.

**Acceptance Scenarios**:

1. **Given** an existing BOM ID with 5 component items, **When** a user requests `GET /api/boms/{id}`, **Then** the system returns HTTP 200 with header details (ID, product ID, version, status ID, createdBy, createdAt) and the array of 5 component items (`materialProductId`, `quantityPerUnit`, `unit`, `scrapRate`).
2. **Given** a non-existent BOM ID, **When** a user requests `GET /api/boms/{invalid_id}`, **Then** the system returns HTTP 404 Not Found error.

---

### Edge Cases

- What happens if `page` or `size` are invalid (e.g. negative)? The system defaults `page = 0` and `size = 20`.
- What happens if a BOM has 0 items? Get BOM detail returns an empty `items: []` array.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide a paginated search API `GET /api/boms` returning `PageResponse<BomDto>`.
- **FR-002**: The `GET /api/boms` endpoint MUST support optional query parameters: `page` (default 0), `size` (default 20), `finishedProductId` (optional UUID filter), `bomStatusId` (optional UUID filter).
- **FR-003**: The system MUST provide a single BOM detail retrieval API `GET /api/boms/{id}` returning `ApiResponse<BomDto>`.
- **FR-004**: The `GET /api/boms/{id}` response MUST contain the full array of component items (`items`) linked to that BOM.
- **FR-005**: Both GET endpoints MUST be accessible to roles `ADMIN`, `PLANNER`, `FACTORY_MANAGER`, `OPERATOR`, and `QC_INSPECTOR`.

### Key Entities

- **Bill of Materials (BOM)**: Header entity with `finishedProductId`, `version`, `bomStatusId`, `createdBy`, `createdAt`.
- **BOM Item**: Component line items (`materialProductId`, `quantityPerUnit`, `unit`, `scrapRate`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Response time for paginated list query is under 200ms for 10,000+ total BOM records.
- **SC-002**: Response time for single BOM detail query with 50+ line items is under 100ms.
- **SC-003**: 100% of non-existent BOM ID queries return standard 404 Not Found responses.

## Assumptions

- Users accessing GET endpoints hold at least one authenticated MES role.
- Filtering params are optional; omitting them returns all BOMs paginated.
