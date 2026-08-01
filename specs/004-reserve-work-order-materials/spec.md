# Feature Specification: Reserve Work Order Materials

**Feature Branch**: `004-reserve-work-order-materials`

**Created**: 2026-07-30

**Status**: Draft

**Input**: User description: "Implement endpoint POST /api/work-orders/{id}/reserve-materials"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reserve Materials for Production (Priority: P1)

A Planner reserves all materials required by a Work Order so the order can proceed to production when the required stock is available.

**Why this priority**: Material reservation is the gate between planning and production. It prevents the factory from starting a Work Order without secured material.

**Independent Test**: Can be fully tested by submitting a valid Work Order ID for a planned order with sufficient stock in the configured raw material warehouse.

**Acceptance Scenarios**:

1. **Given** a Work Order is in `PLANNED` status and all required materials are available in the configured raw material warehouse, **When** the Planner calls `POST /api/work-orders/{id}/reserve-materials`, **Then** the system reserves all required materials, creates the reservation movements, changes the Work Order to `READY_TO_PRODUCE`, and returns `200 OK` with the Work Order ID and `READY_TO_PRODUCE` status.
2. **Given** a Work Order is in `MATERIAL_SHORTAGE` status and stock has been replenished, **When** the Planner calls the reserve endpoint, **Then** the system retries the reservation and changes the Work Order to `READY_TO_PRODUCE` when all material requirements are met.
3. **Given** a material is available in multiple lots, **When** the system reserves that material, **Then** it consumes lots in FIFO order by lot creation time.

---

### User Story 2 - Prevent Partial Reservation on Shortage (Priority: P1)

A Planner receives a clear shortage result when any required material is insufficient, without leaving the inventory or Work Order materials partially reserved.

The system protects shared inventory from competing reservation requests and records the resulting Work Order status transition for audit purposes.

**Why this priority**: Concurrent reservations must not create negative stock, duplicate reservations, or an untraceable production readiness decision.

**Independent Test**: Can be tested by sending concurrent reservation requests against shared stock and verifying that successful reservations never exceed the available quantity and that each important status transition is auditable.

**Acceptance Scenarios**:

1. **Given** 10 units are available and 20 concurrent requests each require 1 unit, **When** all requests execute, **Then** exactly 10 requests succeed, 10 requests fail for insufficient stock, and the final available quantity is zero without negative stock.
2. **Given** a reservation successfully changes a Work Order status, **When** the transaction completes, **Then** an audit record exists for the `RESERVE_MATERIAL` action and the status transition.
3. **Given** a request is unauthenticated or is made by a role other than `PLANNER`, **When** the request is received, **Then** the system returns `401 Unauthorized` or `403 Forbidden` and does not change any data.

---

### Edge Cases

- What happens when the Work Order ID does not exist? The system returns `404 NOT_FOUND` without changing inventory.
- How is machine assignment handled? This operation does not accept or validate a machine; the production-start operation validates the selected machine and its availability.
- What happens when the Work Order is in an invalid lifecycle status? The system rejects the action with `400 INVALID_INPUT` and leaves inventory unchanged.
- What happens when stock exists only in an inactive warehouse? The system excludes that stock and returns `INSUFFICIENT_STOCK` when active warehouses cannot satisfy the reservation.
- What happens when the same request competes with another reservation? The system serializes access, rechecks the current quantity, and either completes the reservation or returns `INSUFFICIENT_STOCK`.
- What happens when a material is split across several lots? The system uses the oldest eligible lots first and creates a movement for each lot used.
- What happens when a retry is submitted after a successful reservation? The system rejects the request because the Work Order is no longer in a reservable status.

## Requirements *(mandatory)*
- What happens when the Work Order is in an invalid lifecycle status? The system rejects the action with `400 INVALID_INPUT` and leaves inventory unchanged.
- What happens when stock exists only in an inactive warehouse? The system excludes that stock and returns `INSUFFICIENT_STOCK` when active warehouses cannot satisfy the reservation.
- What happens when the same request competes with another reservation? The system serializes access, rechecks the current quantity, and either completes the reservation or returns `INSUFFICIENT_STOCK`.
- What happens when a material is split across several lots? The system uses the oldest eligible lots first and creates a movement for each lot used.
- What happens when a retry is submitted after a successful reservation? The system rejects the request because the Work Order is no longer in a reservable status.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST expose `POST /api/work-orders/{id}/reserve-materials` for authenticated users with the `PLANNER` role.
- **FR-003**: System MUST query `AVAILABLE` stock balances across all `ACTIVE` warehouses without requiring a primary warehouse configuration or client warehouse parameter.
- **FR-004**: System MUST consider `AVAILABLE` stock balances for the required material products across all `ACTIVE` warehouses in strict FIFO date order (`stock_lots.created_at ASC`).
- **FR-005**: System MUST calculate or use each Work Order material requirement according to `requiredQuantity = plannedQuantity × quantityPerUnit × (1 + scrapRate)`.
- **FR-006**: When multiple eligible lots contain the same material, the system MUST reserve them in FIFO order based on lot creation time.
- **FR-007**: System MUST allow reservation only when the Work Order status is `PLANNED` or `MATERIAL_SHORTAGE`.
- **FR-008**: System MUST change a Work Order to `READY_TO_PRODUCE` only when all required materials are sufficient.
- **FR-009**: Machine assignment and availability validation are outside this reservation operation and occur when production starts.
- **FR-010**: Reservation MUST be all-or-nothing. If any required material is insufficient across all active warehouses, the system MUST not change stock balances, reserved material quantities, or create reservation movements for any material.
- **FR-011**: On successful reservation, the system MUST move the reserved quantities from `AVAILABLE` to `RESERVED` at each respective warehouse and location, update the Work Order material reservation quantities, and create immutable `RESERVE` stock movements linked to the Work Order, selected lots, warehouse IDs, and location IDs.
- **FR-012**: The system MUST execute the reservation and related Work Order changes as one atomic transaction.
- **FR-013**: The system MUST use pessimistic locking for the stock balances involved in reservation, recheck quantities while locked, and guarantee that stock never becomes negative or reserved beyond available quantity.
- **FR-014**: When stock is insufficient, the system MUST set the Work Order status to `MATERIAL_SHORTAGE` and return `400` with error code `INSUFFICIENT_STOCK`, including the missing materials and shortage quantities.
- **FR-015**: Every important Work Order status transition caused by this operation MUST create an audit record with action `RESERVE_MATERIAL`.
- **FR-016**: A successful request MUST return `200 OK` with message `Materials reserved successfully. Work Order is now READY_TO_PRODUCE.` and data containing `workOrderId` and status `READY_TO_PRODUCE`.
- **FR-017**: The system MUST return `401 Unauthorized` for unauthenticated requests, `403 Forbidden` for non-Planner users, `404 NOT_FOUND` for missing Work Orders, and `400 INVALID_INPUT` for an invalid Work Order state.

### Key Entities

- **Work Order**: The production order whose lifecycle changes from `PLANNED` or `MATERIAL_SHORTAGE` to `READY_TO_PRODUCE` after a successful reservation.
- **Work Order Material**: The material requirement and its required, reserved, and consumed quantities for a Work Order.
- **Warehouse**: Any warehouse in `ACTIVE` status is eligible for this reservation operation; inactive warehouses are excluded.
- **Stock Balance**: The current quantity of a product and lot at a warehouse location and stock status.
- **Stock Lot**: A material lot used to apply FIFO selection during reservation.
- **Stock Movement**: The immutable `RESERVE` record that traces each quantity moved from `AVAILABLE` to `RESERVED`.
- **Audit Log**: The immutable record of the reservation action and important Work Order status transitions.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of valid reservations with sufficient material return `200 OK` and place the Work Order in `READY_TO_PRODUCE`.
- **SC-002**: 100% of shortage reservations leave all stock balances and Work Order material reservation quantities unchanged, except for the required `MATERIAL_SHORTAGE` status result.
- **SC-003**: In the defined 20-request concurrency test with 10 available units, exactly 10 requests succeed and 10 fail, with no negative stock and no duplicate reservation movements.
- **SC-004**: 100% of successful status transitions caused by reservation have a corresponding `RESERVE_MATERIAL` audit record.
- **SC-005**: 100% of reservations include eligible stock from all `ACTIVE` warehouses and exclude stock from inactive warehouses.
- **SC-006**: Planners receive a response identifying every insufficient material and its shortage quantity, allowing them to replenish stock and retry the reservation.

## Assumptions

- The existing authentication system supplies the caller identity and role used for authorization.
- Eligible stock may be distributed across multiple `ACTIVE` warehouses.
- Work Order material requirements are already associated with the Work Order when this action is called.
- The Work Order's BOM and material requirement data remain stable during reservation; changing planning quantities is handled by the Work Order update flow.
- Machine assignment and availability are evaluated by the production-start operation, not by material reservation.
- A successful reservation is not repeated for a Work Order that has already reached `READY_TO_PRODUCE`.
- The exact API response envelope may include the platform-wide response metadata in addition to the specified success message and data fields.
