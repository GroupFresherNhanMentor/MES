# Feature Specification: Inventory Summary Report

**Feature Branch**: `feature/report-001-inventory-summary`

**Created**: 2026-07-29

**Status**: Draft

**Input**: User description: "Inventory Summary Report" (FR-RPT-001)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Inventory Summary Report (Priority: P1)

As a Factory Manager, Admin, or Auditor, I want to view a aggregated summary report of stock levels across products and warehouses so that I can monitor available, reserved, quality-inspection, on-hold, and scrapped stock quantities.

**Why this priority**: Core reporting functionality required for inventory visibility and operational decision-making across all warehouses.

**Independent Test**: Send `GET /api/reports/inventory-summary` with valid authorization; verify HTTP 200 OK with list of aggregated inventory summary rows matching filter parameters.

**Acceptance Scenarios**:

1. **Given** an authorized user (`ADMIN`, `FACTORY_MANAGER`, or `AUDITOR`), **When** sending `GET /api/reports/inventory-summary`, **Then** the system returns HTTP 200 OK with a list of summary rows containing `productCode`, `productName`, `warehouse`, `availableQuantity`, `reservedQuantity`, `qualityInspectionQuantity`, `onHoldQuantity`, `scrappedQuantity`, and `totalOnHand`.
2. **Given** optional query filters (`warehouseId`, `productTypeId`, `productCode`), **When** sending `GET /api/reports/inventory-summary?warehouseId=...`, **Then** the system returns only stock balance rows matching the specified filters.
3. **Given** an unauthorized user (e.g. `OPERATOR` or `MAINTENANCE_ENGINEER`), **When** sending `GET /api/reports/inventory-summary`, **Then** the system rejects the request with HTTP 403 Forbidden.

---

### Edge Cases

- What happens if a warehouse has no stock balances recorded? The report omits empty warehouses or returns an empty list `[]`.
- How are stock status quantities grouped? Quantities are aggregated by `stock_status_id` (AVAILABLE, RESERVED, QUALITY_INSPECTION, ON_HOLD, SCRAPPED) for each product and warehouse pair.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide endpoint `GET /api/reports/inventory-summary` restricted to roles `ADMIN`, `FACTORY_MANAGER`, and `AUDITOR` (`@PreAuthorize("hasAnyRole('ADMIN', 'FACTORY_MANAGER', 'AUDITOR')")`).
- **FR-002**: System MUST support optional query parameters for filtering: `warehouseId` (UUID), `productTypeId` (UUID), and `productCode` (String).
- **FR-003**: System MUST aggregate stock balances grouped by product and warehouse, computing sums for `availableQuantity`, `reservedQuantity`, `qualityInspectionQuantity`, `onHoldQuantity`, `scrappedQuantity`, and total `totalOnHand`.
- **FR-004**: System MUST return HTTP 200 OK with standard `ApiResponse<List<InventorySummaryReportDto>>`.

### Key Entities *(include if feature involves data)*

- **Stock Balance (`stock_balances`)**: Source table for current stock quantities by warehouse, location, product, lot, and status.
- **Product (`products`)**: Source table for product details (`code`, `name`, `product_type_id`).
- **Warehouse (`warehouses`)**: Source table for warehouse details (`name`, `code`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Report query execution completes within < 100ms even with thousands of stock balance records.
- **SC-002**: Role access control correctly enforces permissions (HTTP 403 for unauthorized roles).
- **SC-003**: Sum of status quantities (`available` + `reserved` + `QI` + `onHold` + `scrapped`) strictly equals `totalOnHand` for each summary row.

## Assumptions

- Stock status names in database correspond to standard states: `AVAILABLE`, `RESERVED`, `QUALITY_INSPECTION`, `ON_HOLD`, `SCRAPPED`.
- Read-only query; no database state is mutated during report execution.
