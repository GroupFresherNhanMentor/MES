# Feature Specification: Create Work Order (`POST /api/v1/work-orders`)

**Feature Branch**: `wo-002-create-work-order`

**Created**: 2026-07-29

**Status**: Draft

**Input**: User description: "Implement POST Work Order"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create Work Order with Active BOM & Calculate Requirements (Priority: P1) 🎯 MVP

As a Production Planner, I want to create a new Work Order for a finished product so that the system automatically binds the active Bill of Materials (BOM) and calculates the required quantity for each material component.

**Why this priority**: Creating a Work Order is the entry point of the manufacturing lifecycle. Automatically linking the active BOM and calculating material requirements is essential for production execution and material reservation.

**Independent Test**: Can be tested independently by issuing a `POST /api/v1/work-orders` request with valid payload. Verify that the Work Order record is created with `bomId` populated from the active BOM and `work_order_materials` records are calculated based on `plannedQuantity * quantityPerUnit * (1 + scrapRate)`.

**Acceptance Scenarios**:

1. **Given** a finished product with an ACTIVE BOM, **When** a Planner submits a valid Work Order creation request (`code`, `finishedProductId`, `plannedQuantity > 0`, `plannedStartDate`, `plannedEndDate`), **Then** the system creates the Work Order with status `PLANNED` (or `DRAFT`), links the active `bomId`, calculates and saves `work_order_materials`, and returns HTTP 201 Created with the created Work Order DTO.
2. **Given** a finished product without any ACTIVE BOM, **When** a Planner attempts to create a Work Order, **Then** the system rejects the request with HTTP 400 Bad Request and error code `BOM_NOT_ACTIVE`.

---

### User Story 2 - Role Access Control & Audit Logging (Priority: P2)

As a Factory Auditor/Manager, I want all Work Order creation attempts to be restricted to authorized roles (PLANNER) and automatically logged to the audit log system so that operational actions are fully traceable.

**Why this priority**: Ensures system security and compliance with factory governance rules by enforcing role-based permissions and audit trails.

**Independent Test**: Send `POST /api/v1/work-orders` with unauthorized roles (e.g. ADMIN, OPERATOR, GUEST) and verify HTTP 403 Forbidden. After a successful creation by a PLANNER, verify an audit log record with `action = "CREATE_WORK_ORDER"` is written.

**Acceptance Scenarios**:

1. **Given** an authenticated user with `PLANNER` role, **When** creating a Work Order, **Then** the request succeeds and an audit log entry with `action = "CREATE_WORK_ORDER"` is recorded.
2. **Given** an authenticated user with `ADMIN` role, **When** attempting to create a Work Order, **Then** the system returns HTTP 403 Forbidden.
3. **Given** an unauthenticated request, **When** attempting to create a Work Order, **Then** the system returns HTTP 401 Unauthorized.

---

### User Story 3 - Validation & Duplicate Prevention (Priority: P3)

As a Production Planner, I want clear validation error messages when entering invalid data or duplicate Work Order codes so that errors can be corrected immediately.

**Why this priority**: Prevents bad data entry, duplicate work orders, and invalid date ranges.

**Independent Test**: Submit Work Order payloads with `plannedQuantity <= 0`, missing mandatory fields, duplicate `code`, or `plannedEndDate` before `plannedStartDate`, and verify appropriate HTTP 400 validation error responses.

**Acceptance Scenarios**:

1. **Given** an existing Work Order code "WO-2026-0001", **When** creating another Work Order with code "WO-2026-0001", **Then** the system returns HTTP 400 Bad Request with error code `WORK_ORDER_CODE_EXISTS`.
2. **Given** a request with `plannedQuantity = -5`, **When** submitted, **Then** the system returns HTTP 400 Bad Request with input validation error details.

---

### Edge Cases

- What happens if the active BOM for the product has 0 material items? The Work Order is created, but no `work_order_materials` items are inserted.
- How does the system handle concurrent creation requests with the same code? The database UNIQUE constraint on `code` ensures one succeeds and the other fails with a duplicate key exception converted to HTTP 400.
- What if `plannedEndDate` is earlier than `plannedStartDate`? System returns HTTP 400 validation error.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST restrict `POST /api/v1/work-orders` to authenticated users with `ROLE_PLANNER`.
- **FR-002**: System MUST validate that `plannedQuantity > 0`, `code` is not blank, `finishedProductId` is provided, and `plannedEndDate` is after `plannedStartDate`.
- **FR-003**: System MUST verify that a BOM with status `ACTIVE` exists for the given `finishedProductId`. If no active BOM exists, system MUST return error response with error code `BOM_NOT_ACTIVE`.
- **FR-004**: System MUST automatically bind the active `bomId` to the newly created Work Order.
- **FR-005**: System MUST calculate material requirements for each item in the active BOM using the formula `requiredQuantity = plannedQuantity * quantityPerUnit * (1 + scrapRate)` and save them into `work_order_materials`.
- **FR-006**: System MUST initialize the Work Order status to `PLANNED` (or `DRAFT`).
- **FR-007**: System MUST enforce unique Work Order `code` across the system.
- **FR-008**: System MUST write an audit log entry with action `CREATE_WORK_ORDER` containing details of the created Work Order.

### Key Entities *(include if feature involves data)*

- **WorkOrder**: Represents a manufacturing work order containing `id`, `code`, `finishedProductId`, `bomId`, `plannedQuantity`, `plannedStartDate`, `plannedEndDate`, `priorityId`, `workOrderStatusId`, `createdBy`, `createdAt`.
- **WorkOrderMaterial**: Represents required material components for a Work Order containing `id`, `workOrderId`, `materialProductId`, `requiredQuantity`, `reservedQuantity`, `consumedQuantity`.
- **AuditLog**: System audit log record recording `action = "CREATE_WORK_ORDER"`, `performedBy`, `details`, and `timestamp`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of created Work Orders automatically link to the active BOM and correctly calculate material quantities according to the scrap rate formula.
- **SC-002**: Creation requests for finished products without an ACTIVE BOM consistently fail with error code `BOM_NOT_ACTIVE`.
- **SC-003**: Work Order creation requests are processed and persisted in under 500 milliseconds.
- **SC-004**: 100% of successful Work Order creations generate an audit log record with `action = "CREATE_WORK_ORDER"`.

## Assumptions

- The BOM module provides a repository/use-case method `findActiveBomByFinishedProductId(UUID finishedProductId)` to fetch the current active BOM.
- The `work_order_statuses` table contains seed record for `PLANNED` (or `DRAFT`).
- Current authenticated user's ID is extracted from the Spring Security authentication principal (`AppUserPrincipal` / JWT token).
