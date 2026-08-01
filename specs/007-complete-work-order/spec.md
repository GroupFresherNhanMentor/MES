# Feature Specification: Complete Work Order

**Feature Branch**: `007-complete-work-order`

**Created**: 2026-08-01

**Status**: Draft

**Input**: User description: "Implement POST /api/v1/work-orders/{id}/complete"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Complete Production and Record Classified Output (Priority: P1)

As a Production Operator, I want to complete an in-progress Work Order by recording actual, good, defective, and scrap quantities plus the destination warehouse location, so that the production run is closed and finished output is available for quality review.

**Why this priority**: Completing production is the final shop-floor action that records actual performance and makes produced items available to the quality process.

**Independent Test**: Can be fully tested with an in-progress Work Order that has an active production run and reserved materials. Submit valid quantities and a destination warehouse location, then verify the Work Order is completed, the run is closed, and distinct good and defective output lots await quality review.

**Acceptance Scenarios**:

1. **Given** an `IN_PROGRESS` Work Order with an active production run and valid quantities, **When** an Operator completes it, **Then** the Work Order becomes `COMPLETED`, the run records its end time and reported quantities, the assigned machine becomes `AVAILABLE`, and a `COMPLETE` event is recorded.
2. **Given** actual quantity 100, good quantity 90, defect quantity 5, and scrap quantity 5, **When** an Operator completes the Work Order, **Then** the system creates two independent finished-goods lots at the requested output location: one `QUALITY_INSPECTION` balance of 90 good units and one `QUALITY_INSPECTION` balance of 5 defective units.
3. **Given** a completed production request contains a note, **When** completion succeeds, **Then** the note is retained with the completion event.

---

### User Story 2 - Consume and Release Reserved Materials Accurately (Priority: P1)

As a Production Operator, I want material consumption to be proportional to actual output and unused reservations returned to available stock, so that inventory and material-loss accounting reflect the real production outcome.

**Why this priority**: Accurate raw-material consumption, scrap allocation, and release of unused stock protect inventory integrity and production cost reporting.

**Independent Test**: Can be fully tested from a Work Order whose material reservations span multiple lots by completing with an actual quantity lower than planned quantity and verifying per-lot consumption, scrap, and release movements.

**Acceptance Scenarios**:

1. **Given** a material line has reserved quantity 100, planned quantity 100, and actual quantity 80, **When** the Work Order is completed, **Then** 80 units are consumed and the remaining 20 units are returned from `RESERVED` to `AVAILABLE`.
2. **Given** a material allocation consumes 80 units and production scrap is 5 of actual quantity 100, **When** the Work Order is completed, **Then** 4 units are recorded as material scrap and 76 units as normal production consumption for that allocation.
3. **Given** actual quantity exceeds planned quantity, **When** the Work Order is completed, **Then** material consumption is capped at the quantity reserved for each material and no reserved balance becomes negative.
4. **Given** actual, good, defect, and scrap quantities are all zero, **When** the Work Order is completed, **Then** no material is consumed or scrapped and all reserved material is released to `AVAILABLE`.

---

### User Story 3 - Protect Completion Authorization and Integrity (Priority: P1)

As a Factory Manager, I want only Operators to complete a Work Order and completion to execute as one consistent business action, so that finalized production records cannot be duplicated or partially applied.

**Why this priority**: Completion changes production, inventory, quality, machine availability, and audit data simultaneously; unauthorized or duplicate completion would corrupt operations.

**Independent Test**: Can be tested by invoking completion as different roles, attempting completion from invalid Work Order states, and submitting two simultaneous completion requests for the same Work Order.

**Acceptance Scenarios**:

1. **Given** an authenticated `OPERATOR`, **When** the Operator completes an eligible Work Order, **Then** the completion is accepted.
2. **Given** an authenticated user without the `OPERATOR` role, **When** the user attempts completion, **Then** the system denies the request without changing data.
3. **Given** a Work Order not in `IN_PROGRESS`, **When** an Operator attempts completion, **Then** the system rejects the invalid lifecycle transition without changing data.
4. **Given** two Operators attempt to complete the same in-progress Work Order simultaneously, **When** both requests are processed, **Then** exactly one completion succeeds and only one set of output, material movements, events, and audit records exists.

---

### Edge Cases

- A completion request for a non-existent Work Order returns a not-found result and makes no changes.
- `actualQuantity`, `goodQuantity`, `defectQuantity`, and `scrapQuantity` cannot be negative.
- `goodQuantity + defectQuantity + scrapQuantity` must equal `actualQuantity`.
- A request must identify an existing output warehouse and a location belonging to that warehouse.
- Completion is rejected when there is no active production run or no active lifecycle transition from `IN_PROGRESS` to `COMPLETED`.
- Zero-quantity good or defective classifications still create their separate output lot, quality-isolation balance, and quality inspection record.
- If any material, output, quality, run, machine, event, or audit update cannot be completed, no part of the completion takes effect.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide `POST /api/v1/work-orders/{id}/complete` to authenticated users with only the `OPERATOR` role.
- **FR-002**: The completion request MUST include `actualQuantity`, `goodQuantity`, `defectQuantity`, `scrapQuantity`, `outputWarehouseId`, and `outputLocationId`; it MAY include a completion note.
- **FR-003**: System MUST reject negative reported quantities and MUST require `goodQuantity + defectQuantity + scrapQuantity = actualQuantity`.
- **FR-004**: System MUST allow completion only when the Work Order is `IN_PROGRESS`, has an active production run, and has an active configured transition from `IN_PROGRESS` to `COMPLETED`.
- **FR-005**: For every Work Order material, system MUST calculate production consumption as `min(reservedQuantity x actualQuantity / plannedQuantity, reservedQuantity)`.
- **FR-006**: System MUST apply material consumption against the original reserved material lots without exceeding any reserved balance, and MUST update each Work Order material's consumed quantity by its total consumed amount.
- **FR-007**: For each consumed material-lot allocation when actual quantity is greater than zero, system MUST calculate scrap material quantity as `consumedAllocationQuantity x scrapQuantity / actualQuantity`; the remainder is normal production consumption.
- **FR-008**: System MUST record normal material consumption as `CONSUME_IN_PRODUCTION` and material scrap as `SCRAP`; together, these records MUST equal the consumed allocation quantity and must not deduct the same reserved stock twice.
- **FR-009**: System MUST return every reserved quantity not consumed during completion from `RESERVED` to `AVAILABLE` and record the return as `RELEASE_RESERVATION`, preserving the original material lot and warehouse location.
- **FR-010**: System MUST create separate finished-goods lots and separate `QUALITY_INSPECTION` balances for good quantity and defect quantity at the requested output warehouse and location, including when either classified quantity is zero.
- **FR-011**: System MUST record a `PRODUCTION_OUTPUT` movement for each good or defective finished-goods lot with a positive quantity and MUST create a separate pending quality inspection for each lot, allowing inspection quantity zero.
- **FR-012**: System MUST not create a finished-goods stock balance for scrap quantity.
- **FR-013**: System MUST close the active production run by recording end time, actual quantity, good quantity, defect quantity, and scrap quantity.
- **FR-014**: System MUST record a `COMPLETE` Work Order event associated with the completed production run and retain the optional note.
- **FR-015**: System MUST set the Work Order status to `COMPLETED`, set the production machine to `AVAILABLE`, and create an audit entry with action `COMPLETE_PRODUCTION` only after all completion effects succeed.
- **FR-016**: System MUST apply all completion effects as one atomic operation. A failure in any effect MUST leave the Work Order, production run, machine, stock, output lots, quality inspections, events, and audit history unchanged.
- **FR-017**: System MUST prevent duplicate completion of the same Work Order under concurrent requests; at most one request may finalize a production run and create completion effects.
- **FR-018**: System MUST return an appropriate authorization result for unauthenticated and non-Operator requests, a not-found result for missing Work Orders, and a validation result for invalid quantities, output destinations, runs, or lifecycle states.

### Key Entities

- **Work Order**: Production order whose lifecycle can move from `IN_PROGRESS` to `COMPLETED`.
- **Production Run**: Active execution record that stores machine, timing, and actual, good, defective, and scrap results.
- **Work Order Material**: Required material line that tracks reserved and consumed quantities.
- **Stock Balance**: Quantity of a product lot at a warehouse location in a defined physical status.
- **Stock Movement**: Immutable trace of normal consumption, material scrap, reservation release, and finished-goods output linked to the Work Order.
- **Finished-Goods Lot**: Independently traceable good or defective output placed in quality isolation.
- **Quality Inspection**: Pending quality review for one finished-goods output lot, including a zero-quantity classification when applicable.
- **Work Order Event and Audit Entry**: Immutable records of the completion action, note, actor, and lifecycle transition.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An Operator can submit a valid completion and receive the finalized Work Order result within 1 second under normal operating load.
- **SC-002**: 100% of successful completions record quantities whose good, defective, and scrap total equals actual quantity and close exactly one production run.
- **SC-003**: 100% of successful completions preserve material accounting: consumed plus released quantity equals the quantity reserved for every material line.
- **SC-004**: 100% of successful completions create distinct, traceable quality-isolation output records for good and defective quantities.
- **SC-005**: In simultaneous completion attempts for one Work Order, exactly one request succeeds and no duplicate material, output, quality, event, or audit records are created.
- **SC-006**: 100% of rejected completion requests leave production, inventory, quality, machine, and audit data unchanged.

## Assumptions

- The authenticated identity supplies the Operator role and actor identity.
- The existing configured lifecycle transition `IN_PROGRESS` to `COMPLETED` is enabled before completion is attempted.
- Each in-progress Work Order has one active production run and one assigned production machine.
- The requested output location belongs to the requested output warehouse and is eligible to hold finished goods.
- Good and defective output must remain in `QUALITY_INSPECTION` until quality staff disposition them; production scrap is a material-loss record and is not finished-goods inventory.
- The existing finished-goods lot type used for internally produced output is available.
