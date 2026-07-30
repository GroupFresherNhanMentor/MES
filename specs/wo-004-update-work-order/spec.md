# Feature Specification: Update Work Order

**Feature Branch**: `feat/POST-GET-WO-final`

**Created**: 2026-07-30

**Status**: Draft

**Input**: User description: "Implement endpoint PUT /api/v1/work-orders/{id}"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Update Planning Fields of a Work Order (Priority: P1)

A Planner needs to modify the planning details of an existing Work Order that is still in the early planning phase (DRAFT or PLANNED). They update fields such as the work order code, planned quantity, planned start/end dates, and priority level.

**Why this priority**: This is the core purpose of the PUT endpoint — allowing Planners to refine work order details before production begins. Without this, Planners cannot correct mistakes or adjust plans.

**Independent Test**: Can be fully tested by sending a PUT request with updated fields for a Work Order in DRAFT status and verifying the response contains the updated values.

**Acceptance Scenarios**:

1. **Given** a Work Order exists in DRAFT status, **When** the Planner sends a PUT request with a new `plannedQuantity` of 200 and valid date range, **Then** the system updates the Work Order and returns 200 OK with the updated WorkOrderDto.
2. **Given** a Work Order exists in PLANNED status, **When** the Planner updates the `code`, `priorityId`, and `plannedEndDate`, **Then** the system persists all changes and returns the updated WorkOrderDto.
3. **Given** a Work Order exists in DRAFT status with existing materials, **When** the Planner changes the `plannedQuantity`, **Then** the system recalculates `requiredQuantity` for all associated materials using the formula: `requiredQuantity = plannedQuantity × quantityPerUnit × (1 + scrapRate)`.

---

### User Story 2 - Simple Status Transition via PUT (Priority: P1)

A Planner transitions a Work Order between the two planning states (DRAFT and PLANNED) using the PUT endpoint by including a `workOrderStatusId` in the request body.

**Why this priority**: The ability to promote a draft to planned (or revert a planned order back to draft) is essential for the planning workflow and is tightly coupled with the update operation.

**Independent Test**: Can be tested by sending a PUT request with a `workOrderStatusId` corresponding to PLANNED for a Work Order currently in DRAFT, and verifying the status change in the response.

**Acceptance Scenarios**:

1. **Given** a Work Order in DRAFT status, **When** the Planner sends a PUT with `workOrderStatusId` set to the PLANNED status ID, **Then** the system transitions the Work Order to PLANNED and returns 200 OK.
2. **Given** a Work Order in PLANNED status, **When** the Planner sends a PUT with `workOrderStatusId` set to the DRAFT status ID, **Then** the system transitions the Work Order back to DRAFT and returns 200 OK.
3. **Given** a Work Order in DRAFT status, **When** the Planner sends a PUT with `workOrderStatusId` set to IN_PROGRESS, **Then** the system rejects the request with 400 Bad Request and an appropriate error message indicating that operational transitions must use dedicated action endpoints.

---

### User Story 3 - Input Validation Guards (Priority: P2)

The system enforces strict validation rules on the update request to ensure data integrity: planned quantity must be positive, and the start date must precede the end date.

**Why this priority**: Validation prevents corrupted data from entering the system. While the system functions without it, bad data causes downstream failures in material calculation and production scheduling.

**Independent Test**: Can be tested by sending PUT requests with invalid field values (zero quantity, end date before start date) and verifying the system returns 400 Bad Request with descriptive error messages.

**Acceptance Scenarios**:

1. **Given** a Work Order in DRAFT status, **When** the Planner sends a PUT with `plannedQuantity` of 0, **Then** the system returns 400 Bad Request with error code `INVALID_INPUT`.
2. **Given** a Work Order in DRAFT status, **When** the Planner sends a PUT with `plannedQuantity` of -5, **Then** the system returns 400 Bad Request with error code `INVALID_INPUT`.
3. **Given** a Work Order in DRAFT status, **When** the Planner sends a PUT with `plannedStartDate` set to 2026-08-15 and `plannedEndDate` set to 2026-08-10, **Then** the system returns 400 Bad Request with a message indicating the start date must precede the end date.

---

### User Story 4 - Reject Updates for Non-Planning States (Priority: P2)

When a Work Order has progressed beyond the planning phase (e.g., it is in READY_TO_PRODUCE, IN_PROGRESS, PAUSED, COMPLETED, or CANCELLED), the system rejects any update attempt via PUT to prevent uncontrolled modifications to active or completed production orders.

**Why this priority**: This guard ensures operational integrity — once a Work Order enters production or is cancelled, its planning fields should not be modified through the generic update endpoint.

**Independent Test**: Can be tested by attempting to PUT-update a Work Order in each non-planning status and verifying the system returns 400 Bad Request.

**Acceptance Scenarios**:

1. **Given** a Work Order in READY_TO_PRODUCE status, **When** the Planner sends a PUT request to change `plannedQuantity`, **Then** the system returns 400 Bad Request indicating the Work Order cannot be modified in its current state.
2. **Given** a Work Order in IN_PROGRESS status, **When** any user sends a PUT request, **Then** the system returns 400 Bad Request.
3. **Given** a Work Order in COMPLETED status, **When** any user sends a PUT request, **Then** the system returns 400 Bad Request.
4. **Given** a Work Order in CANCELLED status, **When** any user sends a PUT request, **Then** the system returns 400 Bad Request.

---

### User Story 5 - Unique Code Enforcement (Priority: P2)

When a Planner updates the `code` field of a Work Order, the system checks that no other Work Order already uses that code. This ensures each Work Order has a globally unique identifier.

**Why this priority**: Code uniqueness prevents confusion and data integrity issues when tracking Work Orders across the factory floor.

**Independent Test**: Can be tested by updating a Work Order's code to match an existing Work Order's code and verifying the system returns a conflict error.

**Acceptance Scenarios**:

1. **Given** Work Order A has code "WO-001" and Work Order B has code "WO-002", **When** the Planner updates Work Order B's code to "WO-001", **Then** the system returns 400 Bad Request with error code `WORK_ORDER_CODE_EXISTS`.
2. **Given** a Work Order has code "WO-001", **When** the Planner sends a PUT keeping the same code "WO-001" unchanged, **Then** the system accepts the update without a conflict error.

---

### User Story 6 - Authorization Enforcement (Priority: P2)

Only users with the ADMIN or PLANNER role can update Work Orders. Users with other roles (OPERATOR, FACTORY_MANAGER, AUDITOR) are denied access.

**Why this priority**: Access control is essential for security but relies on the existing authorization infrastructure already in place.

**Independent Test**: Can be tested by sending PUT requests with JWT tokens for different roles and verifying access is granted or denied accordingly.

**Acceptance Scenarios**:

1. **Given** a user with the PLANNER role, **When** they send a PUT request to update a Work Order, **Then** the system processes the request.
2. **Given** a user with the ADMIN role, **When** they send a PUT request to update a Work Order, **Then** the system processes the request.
3. **Given** a user with the OPERATOR role, **When** they send a PUT request to update a Work Order, **Then** the system returns 403 Forbidden.
4. **Given** an unauthenticated request (no JWT), **When** a PUT request is sent, **Then** the system returns 401 Unauthorized.

---

### Edge Cases

- What happens when the Work Order ID does not exist? → System returns 404 Not Found with error code `NOT_FOUND`.
- What happens when `workOrderStatusId` is omitted from the request body? → System keeps the current status unchanged and only updates the other provided fields.
- What happens when all fields in the request body are null/absent? → System treats it as a no-op and returns the existing Work Order unchanged with 200 OK.
- What happens when `plannedQuantity` is updated but the Work Order has no associated materials yet? → System updates the quantity on the Work Order record; no material recalculation is needed since there are no materials to recalculate.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow users with ADMIN or PLANNER role to update a Work Order via `PUT /api/v1/work-orders/{id}`.
- **FR-002**: System MUST reject the request with 404 Not Found if the Work Order ID does not exist.
- **FR-003**: System MUST only allow updates when the Work Order is in DRAFT or PLANNED status. Any other status MUST result in 400 Bad Request.
- **FR-004**: System MUST validate that `plannedQuantity > 0` when provided. Violation results in 400 Bad Request (`INVALID_INPUT`).
- **FR-005**: System MUST validate that `plannedStartDate < plannedEndDate` when both are provided. Violation results in 400 Bad Request (`INVALID_INPUT`).
- **FR-006**: System MUST enforce unique `code` across all Work Orders. If the new code already exists on a different Work Order, system returns 400 Bad Request (`WORK_ORDER_CODE_EXISTS`).
- **FR-007**: System MUST only permit status transitions `DRAFT → PLANNED` and `PLANNED → DRAFT` via the PUT endpoint. Any other `workOrderStatusId` value MUST be rejected with 400 Bad Request.
- **FR-008**: System MUST recalculate `requiredQuantity` for all associated materials when `plannedQuantity` changes, using the formula: `requiredQuantity = plannedQuantity × quantityPerUnit × (1 + scrapRate)`.
- **FR-009**: System MUST return 403 Forbidden for users without ADMIN or PLANNER role.
- **FR-010**: System MUST return the full updated WorkOrderDto (including materials and events) in the response body on success.

### Key Entities

- **Work Order**: The central production order entity. Key attributes: `id`, `code`, `plannedQuantity`, `plannedStartDate`, `plannedEndDate`, `priorityId`, `workOrderStatusId`, `finishedProductId`, `bomId`.
- **Work Order Material**: Materials required for a Work Order. Key attributes: `id`, `workOrderId`, `rawMaterialId`, `requiredQuantity`, `reservedQuantity`, `consumedQuantity`. Recalculated when `plannedQuantity` changes.
- **Work Order Status**: Defines the lifecycle state of a Work Order. The PUT endpoint only handles transitions between DRAFT and PLANNED states.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Planners can update all planning fields of a DRAFT or PLANNED Work Order and see changes reflected immediately in the response.
- **SC-002**: The system rejects 100% of update attempts on Work Orders not in DRAFT or PLANNED status.
- **SC-003**: Invalid inputs (zero/negative quantity, reversed date range, duplicate code) are caught and reported with clear error messages before any data is persisted.
- **SC-004**: When planned quantity changes, all associated material requirements are recalculated accurately using the BOM formula.
- **SC-005**: Users without ADMIN or PLANNER role are denied access with appropriate error responses.

## Assumptions

- The existing Work Order module already supports `findById`, `save`, `findMaterialsByWorkOrderId` operations in the persistence layer.
- Work Order status IDs are UUIDs referencing the `work_order_statuses` seed data table. The system can look up the status name (DRAFT, PLANNED, etc.) by ID.
- The BOM items (`quantityPerUnit`, `scrapRate`) needed for material recalculation are accessible from the existing BOM persistence layer.
- The `UpdateWorkOrderRequest` DTO already exists with fields: `code`, `plannedQuantity`, `plannedStartDate`, `plannedEndDate`, `priorityId`, `workOrderStatusId`.
- Partial updates are supported: if a field is `null` in the request, the existing value is preserved.
