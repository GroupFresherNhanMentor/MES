# Feature Specification: Release and Cancel Work Orders

**Feature Branch**: `005-release-cancel-work-orders`

**Created**: 2026-07-30

**Status**: Draft

**Input**: User description: "Implement endpoints POST /api/v1/work-orders/{id}/release-materials and POST /api/v1/work-orders/{id}/cancel"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Release Reserved Materials (Priority: P1)

As a Production Planner, I want to release reserved raw materials from a planned or ready Work Order back into available warehouse inventory, so that other priority work orders can utilize the materials when production plans change.

**Why this priority**: Material reservation locks inventory. Releasing reserved materials prevents inventory starvation and enables flexible production re-scheduling.

**Independent Test**: Can be fully tested by creating a Work Order with reserved materials, calling `POST /api/v1/work-orders/{id}/release-materials`, and verifying that reserved quantity drops to 0, available stock balance increases by the released amount, and a `RELEASE_RESERVATION` stock movement log is generated.

**Acceptance Scenarios**:

1. **Given** a Work Order with 200 units of Material A in `RESERVED` stock status and Work Order status `READY_TO_PRODUCE`, **When** the Planner calls `POST /api/v1/work-orders/{id}/release-materials`, **Then** the reserved quantity for Material A decreases by 200, the available quantity increases by 200 in `stock_balances`, the Work Order material reserved quantity is reset to 0, and a `RELEASE_RESERVATION` stock movement is created.
2. **Given** a Work Order that has reserved materials across multiple distinct stock lots, **When** the Planner releases materials, **Then** the system queries past `RESERVE` stock movements for this Work Order and returns stock to available status across each original lot and location.
3. **Given** a Work Order in `IN_PROGRESS` or `COMPLETED` status, **When** the Planner attempts to call `POST /api/v1/work-orders/{id}/release-materials`, **Then** the system rejects the request with an error indicating that materials cannot be released after production has commenced.

---

### User Story 2 - Cancel Work Order and Auto-Release Materials (Priority: P1)

As a Production Planner, I want to cancel an unstarted Work Order and have all its reserved materials automatically released back to available inventory in a single operation, so that cancelled orders do not leave inventory tied up.

**Why this priority**: Work order cancellation is a common operational event. Auto-releasing materials during cancellation guarantees atomic state cleanup and prevents phantom stock locks.

**Independent Test**: Can be fully tested by taking a `PLANNED` or `READY_TO_PRODUCE` Work Order with reserved materials, executing `POST /api/v1/work-orders/{id}/cancel`, and verifying the Work Order status changes to `CANCELLED` while all reserved materials are returned to available status.

**Acceptance Scenarios**:

1. **Given** a `READY_TO_PRODUCE` Work Order with reserved materials, **When** the Planner calls `POST /api/v1/work-orders/{id}/cancel`, **Then** the Work Order status transitions to `CANCELLED`, all reserved materials are credited back to `AVAILABLE` stock balance, and `RELEASE_RESERVATION` stock movements are created.
2. **Given** a `DRAFT` or `PLANNED` Work Order with zero reserved materials, **When** the Planner calls `POST /api/v1/work-orders/{id}/cancel`, **Then** the Work Order transitions to `CANCELLED` successfully without generating unnecessary stock movements.
3. **Given** a Work Order already in `IN_PROGRESS` or `COMPLETED` status, **When** the Planner attempts to cancel the Work Order, **Then** the system rejects the request with an invalid state transition error.

---

### User Story 3 - Role-Based Access and Concurrency Protection (Priority: P2)

As a System Administrator, I want material release and order cancellation restricted to authorized roles and protected against concurrent race conditions, so that inventory balances remain consistent and audit-compliant.

**Why this priority**: Protects inventory data integrity against race conditions when multiple planners manage overlapping stock concurrently.

**Independent Test**: Test with unauthorized role tokens (expecting HTTP 403) and test parallel execution of release/cancel on the same Work Order (expecting atomic execution without negative stock balances).

**Acceptance Scenarios**:

1. **Given** an authenticated user with role `OPERATOR`, **When** the user attempts to call release or cancel endpoints, **Then** the system returns HTTP 403 Forbidden.
2. **Given** two concurrent release requests for the same Work Order, **When** executed simultaneously, **Then** database locking guarantees exactly one transaction succeeds without causing negative stock balances or duplicate movement logs.

---

### Edge Cases

- What happens if a Work Order has partially consumed materials before cancellation? Cancellation must be blocked if any material consumption has occurred (`IN_PROGRESS` state).
- What happens if `release-materials` is called on a Work Order that has no reserved materials (`reservedQuantity == 0`)? The endpoint completes gracefully as a no-op returning HTTP 200 with an informative message.
- What happens if a referenced Work Order ID does not exist? System returns HTTP 404 Not Found (`RESOURCE_NOT_FOUND`).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide endpoint `POST /api/v1/work-orders/{id}/release-materials` accessible only to users with role `PLANNER` or `ADMIN`.
- **FR-002**: System MUST provide endpoint `POST /api/v1/work-orders/{id}/cancel` accessible only to users with role `PLANNER` or `ADMIN`.
- **FR-003**: System MUST reject material release or order cancellation requests for Work Orders in `IN_PROGRESS` or `COMPLETED` status with HTTP 400 Bad Request.
- **FR-004**: System MUST return reserved stock balances to available stock status upon material release, preserving multi-lot traceability by matching previous `RESERVE` stock movement logs for the specified Work Order.
- **FR-005**: System MUST record a new `stock_movements` log with movement type `RELEASE_RESERVATION` for every material lot released.
- **FR-006**: System MUST update `work_order_materials.reserved_quantity` to reflect released quantities.
- **FR-007**: System MUST automatically release all reserved materials when a Work Order is cancelled via `POST /api/v1/work-orders/{id}/cancel`.
- **FR-008**: System MUST transition Work Order status to `CANCELLED` upon successful execution of the cancel endpoint.
- **FR-009**: System MUST execute stock balance updates, movement logging, and status updates within a single atomic database transaction using row-level locking (`SELECT FOR UPDATE`).
- **FR-010**: System MUST record audit events in `work_order_events` for both material release and cancellation actions.

### Key Entities *(include if feature involves data)*

- **Work Order**: Represents a manufacturing production order (`id`, `code`, `status`, `planned_quantity`).
- **Stock Balance**: Represents warehouse inventory quantities by location, lot, and status (`warehouse_id`, `location_id`, `product_id`, `lot_id`, `stock_status_id`, `quantity`, `version`).
- **Stock Movement**: Historical audit trail of inventory movements (`movement_type_id`, `work_order_id`, `product_id`, `lot_id`, `quantity`, `from_status_id`, `to_status_id`).
- **Work Order Material**: Material requirement lines associated with a Work Order (`work_order_id`, `material_product_id`, `required_quantity`, `reserved_quantity`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Planners can release reserved materials or cancel a Work Order in under 1 second.
- **SC-002**: 100% of material release operations accurately balance stock quantities such that `AVAILABLE` stock increases by exactly the amount `RESERVED` stock decreases.
- **SC-003**: 100% of cancellation operations on reserved Work Orders create corresponding `RELEASE_RESERVATION` stock movement records.
- **SC-004**: System handles concurrent cancellation and material release requests without stock balance corruption or deadlocks.

## Assumptions

- Stock statuses `AVAILABLE` and `RESERVED` exist in seed data.
- Movement type `RELEASE_RESERVATION` exists in seed data (`019facbf-1003-7000-8000-000000000005`).
- Material reservation was performed following standard lot-selection rules, enabling trace back of original lot IDs via `stock_movements`.
