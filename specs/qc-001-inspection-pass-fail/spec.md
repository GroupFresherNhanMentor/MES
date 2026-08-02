# Feature Specification: QC Inspection — Pass / Fail

**Feature Branch**: `001-qc-inspection-pass-fail`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "QC inspection với pass/fail, fail thì kẹt trạng thái, chỉ pass mới được xử lý tiếp"

---

## User Scenarios & Testing

### User Story 1 — QC Inspector passes good lots (Priority: P1)

A QC Inspector opens the inspection list, sees all lots waiting for inspection, selects one, inspects the quantity, and passes the good units. These units become AVAILABLE for downstream use.

**Why this priority**: Core positive flow — without pass, finished goods can never be used.

**Independent Test**: Create a QC inspection via Complete Production, call pass with a quantity, verify stock moves from QUALITY_INSPECTION to AVAILABLE and a QC_RELEASE movement is recorded.

**Acceptance Scenarios**:

1. **Given** a QC inspection with 100 units in PENDING_INSPECTION status, **When** QC Inspector passes 95 units, **Then** 95 units change to PASSED status, stock moves from QUALITY_INSPECTION to AVAILABLE, and a QC_RELEASE stock movement is recorded
2. **Given** a QC inspection with 100 units, **When** QC Inspector passes 120 units, **Then** the system rejects with quantity exceeds remaining error
3. **Given** a QC inspection already fully passed, **When** QC Inspector tries to pass again, **Then** the system rejects with inspection already closed

---

### User Story 2 — QC Inspector fails defective lots (Priority: P1)

A QC Inspector finds defective units during inspection. They record the defect type, reason, and choose an action (SCRAP, HOLD, or REWORK). The failed lot is marked with the corresponding status and stock transitions accordingly. Failed lots are terminal — no further processing is handled in this feature.

**Why this priority**: QC must be able to reject bad units. This is the core negative flow.

**Independent Test**: Create a QC inspection, call fail with action=SCRAP, verify stock moves to SCRAPPED.

**Acceptance Scenarios**:

1. **Given** a QC inspection with 100 units in PENDING_INSPECTION, **When** QC Inspector fails 5 units with action=SCRAP and records a defect reason, **Then** 5 units change to FAILED status, stock moves from QUALITY_INSPECTION to SCRAPPED, and a SCRAP stock movement is recorded
2. **Given** a QC inspection with 100 units, **When** QC Inspector fails 5 units with action=HOLD and a defect reason, **Then** 5 units change to ON_HOLD status, stock moves from QUALITY_INSPECTION to ON_HOLD, and a QC_HOLD stock movement is recorded
3. **Given** a QC inspection with 100 units, **When** QC Inspector fails 5 units with action=REWORK and a defect reason, **Then** 5 units change to REWORK_REQUIRED status, stock stays at QUALITY_INSPECTION, no stock movement is created
4. **Given** a QC inspection, **When** QC Inspector fails without providing defectTypeId and reason, **Then** the system rejects

---

### User Story 3 — QC Inspector partially inspects a lot across multiple sessions (Priority: P2)

A large lot may be inspected in multiple rounds — some units passed now, more passed later, some failed. The inspection remains open until the full quantity is accounted for.

**Why this priority**: Partial inspection is common in real production.

**Independent Test**: Pass 30 then 30 then fail 40, verify the status changes to PASSED or FAILED only after all 100 are accounted for.

**Acceptance Scenarios**:

1. **Given** a QC inspection with 100 units, **When** QC Inspector passes 30, then later passes another 30, then fails 40 with SCRAP, **Then** final status is FAILED, stock: 60 AVAILABLE, 40 SCRAPPED
2. **Given** a QC inspection with 100 units, **When** QC Inspector passes 50, then fails 50 with SCRAP, **Then** inspection fully processed, status = FAILED

---

### User Story 4 — Admin manages QC lookup tables (Priority: P3)

Admin users can create new QC statuses, actions, and defect types. All authenticated users can view them.

**Why this priority**: Reference data management — needed for flexibility but not for core flow.

**Independent Test**: Create a new QC status and verify it appears in the status list.

**Acceptance Scenarios**:

1. **Given** an Admin user, **When** they create a new QC status, **Then** it appears in the status list
2. **Given** an authenticated user, **When** they view QC statuses, actions, or defect types, **Then** the system returns the list

---

### Edge Cases

- pass + fail quantities exceeding inspection total → system rejects
- fail called on fully processed inspection → system rejects with "inspection already closed"
- pass/fail with 0 quantity → system rejects (quantity must be > 0)
- fail with invalid actionId → system rejects
- pass/fail on same inspection concurrently → optimistic locking prevents double-counting

---

## Requirements

### Functional Requirements

- **FR-001**: System MUST allow QC Inspector to view a paginated list of QC inspections, filterable by status, work order, and product
- **FR-002**: System MUST allow QC Inspector to view a single inspection detail including all its results
- **FR-003**: System MUST allow QC Inspector to pass a quantity from a PENDING_INSPECTION inspection, setting status to PASSED, moving stock from QUALITY_INSPECTION to AVAILABLE, and creating a QC_RELEASE stock movement
- **FR-004**: System MUST allow QC Inspector to fail a quantity from a PENDING_INSPECTION inspection with action SCRAP/HOLD/REWORK:
  - SCRAP → FAILED status, stock QUALITY_INSPECTION→SCRAPPED, movement SCRAP
  - HOLD → ON_HOLD status, stock QUALITY_INSPECTION→ON_HOLD, movement QC_HOLD
  - REWORK → REWORK_REQUIRED status, stock stays QUALITY_INSPECTION, no movement
- **FR-005**: System MUST require defectTypeId and reason when failing QC
- **FR-006**: System MUST track remaining quantity as inspection.quantity - SUM(results.quantity). Pass/fail quantities must not exceed remaining
- **FR-007**: System MUST allow partial processing — inspection stays PENDING_INSPECTION until SUM(results.quantity) = inspection.quantity
- **FR-008**: System MUST allow Admin to create new QC statuses, QC actions, and defect types
- **FR-009**: System MUST allow all authenticated users to view QC statuses, QC actions, and defect types
- **FR-010**: System MUST create an audit log for every pass and fail action
- **FR-011**: System MUST auto-create a QC inspection (PENDING_INSPECTION) when a Work Order is completed, with quantity = goodQuantity from production

### Key Entities

- **Quality Inspection**: Batch of finished goods awaiting QC. Contains workOrderId, productId, lotId, quantity, qcStatusId. Tracks remaining quantity via aggregate of its results.
- **Quality Inspection Result**: Each individual pass/fail decision. Contains isPass boolean, quantity, defectTypeId, reason, actionId, inspectorId, inspectedAt.
- **QC Status**: Lookup — PENDING_INSPECTION, PASSED, FAILED, ON_HOLD, REWORK_REQUIRED, SCRAPPED
- **QC Action**: Lookup — SCRAP, HOLD, REWORK
- **Defect Type**: Lookup — SCRATCH, DIMENSION_ERROR, etc.
- **Stock Balance**: Updated on pass/fail — moves between QUALITY_INSPECTION / AVAILABLE / ON_HOLD / SCRAPPED
- **Stock Movement**: Records transitions — QC_RELEASE, QC_HOLD, SCRAP

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: QC Inspector can complete a pass or fail decision within 2 minutes per inspection batch
- **SC-002**: System correctly tracks remaining quantity — partial inspections accumulate correctly without exceeding total
- **SC-003**: 100% of fail decisions have recorded defect type and reason
- **SC-004**: Stock movements created for every pass (QC_RELEASE), scrap (SCRAP), and hold (QC_HOLD)
- **SC-005**: System handles concurrent pass/fail calls on same inspection without data corruption

---

## Assumptions

- Failed lots (SCRAP, HOLD, REWORK) are terminal — no further processing in this feature; downstream handling will be in future features
- Only pass flow (PASSED → stock AVAILABLE) feeds into downstream features like inventory and shipping
- Remaining quantity = inspection.quantity - SUM(results.quantity) — computed at service layer, not a DB column
- REWORK quantity counts as processed (reduces remaining) but stock stays at QUALITY_INSPECTION
- Inspection auto-created by Complete Production; manual POST only for edge cases
- All quantity values use BigDecimal (NUMERIC(18,4))
- Lookup tables seeded with initial data