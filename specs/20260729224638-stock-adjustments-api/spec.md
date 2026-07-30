# Feature Specification: Stock Adjustments API & Approval Workflow

**Feature Branch**: `005-stock-adjustments-api`

**Created**: 2026-07-29

**Status**: Draft

**Input**: User description: "Adjustment phải có reason, Không được làm quantity âm, Adjustment phải tạo stock movement ADJUSTMENT, Nếu adjustment vượt ngưỡng cấu hình thì cần Factory Manager approve. Approve/deny removes record from db."

## Clarifications

### Session 2026-07-29

- Q: How are approved or denied adjustment requests handled in `stock_adjustment_approvals` table? → A: Pending records are deleted/removed from `stock_adjustment_approvals` upon Factory Manager action. Upon approval, the pending record is removed, `StockBalance` is updated, and an `ADJUSTMENT` movement is logged. Upon denial/rejection, the pending record is removed and no balance update or movement is executed.
- Q: What static threshold value should be used to determine if a stock adjustment requires Factory Manager approval? → A: Static threshold of 100 units (|quantityAdjustment| > 100 requires approval).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Standard Stock Adjustment Execution (Priority: P1)

As an inventory manager, I want to perform a stock adjustment (`POST /api/stock-adjustments`) with a mandatory reason so that inventory discrepancies are corrected, valid stock balances are maintained without going negative, and an immutable `ADJUSTMENT` movement transaction is logged.

**Why this priority**: Primary workflow for maintaining real-world inventory accuracy and auditability.

**Independent Test**: Submitting an adjustment within configured threshold with a valid reason, verifying that `StockBalance` is updated and a `StockMovement` with type `ADJUSTMENT` is recorded.

**Acceptance Scenarios**:

1. **Given** a valid product, warehouse, location, and non-blank reason, **When** an adjustment within the threshold is submitted, **Then** the system updates the `StockBalance` quantity, logs an `ADJUSTMENT` stock movement, and returns 201 Created.
2. **Given** a stock adjustment request, **When** the reason is missing or blank, **Then** the system rejects the request with HTTP 400 Bad Request ("Reason is required for stock adjustment").

---

### User Story 2 - Negative Balance Prevention (Priority: P1)

As a warehouse supervisor, I want stock adjustments that would result in negative stock balances to be blocked immediately so that system inventory never drops below zero.

**Why this priority**: Critical domain invariant preventing physical inventory corruption.

**Independent Test**: Submitting a negative adjustment whose absolute value exceeds current on-hand balance and asserting HTTP 400 Bad Request error.

**Acceptance Scenarios**:

1. **Given** an existing stock balance of 10 units, **When** a negative adjustment of -15 units is requested, **Then** the system rejects the request with HTTP 400 Bad Request ("Adjustment cannot result in negative stock balance") without modifying balance or creating movement.

---

### User Story 3 - Threshold Approval Workflow for Large Adjustments (Priority: P2)

As a Factory Manager, I want stock adjustments exceeding a configured threshold to be stored as pending approval requests until I approve or deny them, so that large stock corrections are properly authorized.

**Why this priority**: Financial and operational control over significant inventory changes.

**Independent Test**: Submitting an adjustment greater than the configured threshold, checking that a record is created in `stock_adjustment_approvals`, then approving it as Factory Manager to apply balance update and delete the pending record.

**Acceptance Scenarios**:

1. **Given** a configured adjustment threshold of 100 units, **When** an operator submits an adjustment of 150 units, **Then** the system persists an adjustment record in `stock_adjustment_approvals` table without updating `StockBalance` or finalizing movement.
2. **Given** a pending adjustment record in `stock_adjustment_approvals`, **When** a Factory Manager (`ROLE_FACTORY_MANAGER`) calls `POST /api/stock-adjustments/{id}/approve`, **Then** the system updates `StockBalance`, logs the `ADJUSTMENT` movement, and removes/deletes the record from `stock_adjustment_approvals`.
3. **Given** a pending adjustment record in `stock_adjustment_approvals`, **When** a Factory Manager calls `POST /api/stock-adjustments/{id}/reject`, **Then** the system removes/deletes the record from `stock_adjustment_approvals` without updating `StockBalance` or logging movement.

---

### Edge Cases

- What if an adjustment exceeding threshold is rejected by Factory Manager? System MUST delete the pending adjustment record from `stock_adjustment_approvals` and leave `StockBalance` unchanged.
- What if concurrent adjustments are submitted for the same stock balance while an approval is pending? System MUST lock row during evaluation to ensure eventual balance remains strictly non-negative (`>= 0`).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide an endpoint `POST /api/stock-adjustments` to create inventory adjustment requests.
- **FR-002**: System MUST validate that `reason` is present and non-blank for all stock adjustments.
- **FR-003**: System MUST enforce that post-adjustment stock balance quantity CANNOT be negative (`resultingQuantity >= 0`). Requests violating this constraint MUST be rejected with HTTP 400 Bad Request.
- **FR-004**: System MUST log an immutable transaction entry in `stock_movements` with movement type `ADJUSTMENT` upon completion.
- **FR-005**: System MUST evaluate adjustment magnitude against a static threshold of 100 units (`100.00`):
  - If `|quantityAdjustment| <= 100.00`: Automatically apply balance update and record `ADJUSTMENT` stock movement.
  - If `|quantityAdjustment| > 100.00`: Persist pending request in `stock_adjustment_approvals` without updating `StockBalance`.
- **FR-006**: System MUST provide approval endpoints `POST /api/stock-adjustments/{id}/approve` and `POST /api/stock-adjustments/{id}/reject` restricted to Factory Manager role (`ROLE_FACTORY_MANAGER`).
  - Upon approval (`/approve`), system updates `StockBalance`, logs `ADJUSTMENT` stock movement, and deletes the pending record from `stock_adjustment_approvals`.
  - Upon rejection (`/reject`), system deletes the pending record from `stock_adjustment_approvals` without updating `StockBalance` or logging movement.
- **FR-007**: System MUST include a Flyway database migration creating the `stock_adjustment_approvals` table (`id`, `product_id`, `warehouse_id`, `location_id`, `stock_balance_id`, `quantity_adjustment`, `reason`, `reference_no`, `created_by`, `created_at`) to store pending threshold-exceeding adjustments.

### Key Entities

- **StockAdjustmentRequest**: Payload containing `productId`, `warehouseId`, `locationId`, `stockBalanceId`, `quantityAdjustment`, `reason`, `referenceNo`.
- **StockBalance**: Inventory position tracking quantity per location, product, lot, and status.
- **StockMovement**: Immutable audit ledger recording movement type `ADJUSTMENT`.
- **StockAdjustmentApproval (Table `stock_adjustment_approvals`)**: Queue of pending adjustments waiting for Factory Manager approval. Records are deleted upon approve or reject action.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of stock adjustments require a non-blank reason; requests lacking a reason are rejected with 0 exceptions.
- **SC-002**: Zero stock balance records ever drop below 0 units as a result of adjustments.
- **SC-003**: 100% of pending adjustments in `stock_adjustment_approvals` are deleted upon approval or rejection by Factory Manager.

## Assumptions

- Threshold configuration is defined via application properties (`app.inventory.adjustment-threshold`).
- User role `ROLE_FACTORY_MANAGER` grants authorization to approve or reject threshold-exceeding adjustments.
- Database migration script created under `be/src/main/resources/db/migration/` follows project versioning standard (`V2026...__add_stock_adjustment_approvals.sql`).
