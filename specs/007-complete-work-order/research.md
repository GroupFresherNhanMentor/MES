# Research: Complete Work Order

## Decision: Keep completion persistence inside the Work Order module

**Rationale**: Quality already depends on Work Order. Calling the Quality application service from Work Order would create a reverse module dependency. A Work Order-owned output port can atomically write the related inventory and quality records while keeping business orchestration in `WorkOrderService` and jOOQ details in infrastructure.

**Alternatives considered**:

- Call Inventory and Quality use cases from Work Order: rejected because Quality-to-WorkOrder already exists and would create a cycle.
- Put all completion logic in the controller: rejected because it breaks the application transaction and business-layer rule.

## Decision: Reconstruct reservation allocations from immutable movement history

**Rationale**: Every reservation already creates one `RESERVE` movement with warehouse, location, product, and lot. Completion can deterministically derive outstanding allocation by original lot as reserve minus prior release, consume it in stable order, then release the remaining amount. This is the smallest compatible change and keeps traceability without a new allocation table.

**Alternatives considered**:

- Add a persistent reservation-allocation table: rejected for this feature because the Work Order is finalized after all outstanding reservations are consumed or released; existing movement history is sufficient.
- Consume only aggregated material totals: rejected because it loses lot and location traceability.

## Decision: Split each consumed allocation into normal consumption and material scrap

**Rationale**: For each allocation, calculate `scrapMaterialQuantity = consumedAllocationQuantity x scrapQuantity / actualQuantity`; record the remainder as `CONSUME_IN_PRODUCTION`. The two movements sum exactly to the physical deduction from `RESERVED`, preventing double-deduction while preserving material-loss accounting.

**Alternatives considered**:

- Add a separate SCRAP movement after deducting the complete consumed amount: rejected because it would deduct stock twice.
- Record only aggregate scrap per material: rejected because the requirement demands original material-lot traceability.

## Decision: Create two classified output lots and pending inspections

**Rationale**: Good and defective quantities represent physically distinct output requiring different QC attention. Completion always creates independent good and defective lots, balances in `QUALITY_INSPECTION`, and pending inspections, including zero-quantity classifications. Output movements are only recorded for positive quantities because inventory movements must represent physical movement.

**Alternatives considered**:

- One combined output lot: rejected because it cannot distinguish good from defective output in QC.
- Create output stock for production scrap: rejected because scrap is a material-loss expense, not finished-goods inventory.

## Decision: Permit zero quantity only for quality inspections and classified balances

**Rationale**: The feature requires zero-quantity good or defective classifications to remain traceable. The migration relaxes `quality_inspections.quantity` to non-negative. Existing `stock_balances` already permit zero. `stock_movements.quantity` remains positive, so no zero-quantity `PRODUCTION_OUTPUT` movement is generated.

**Alternatives considered**:

- Relax all movement quantities to non-negative: rejected because zero-valued movements do not represent inventory movement and weaken existing data integrity.
- Skip zero classifications entirely: rejected because it loses the required explicit good/defect classification record.

## Decision: Serialize completion by locking the Work Order and active run

**Rationale**: Locking the Work Order before reading its state ensures only one concurrent request observes `IN_PROGRESS`. Locking the active run and affected reserved balances in a stable order protects finalization and stock integrity. The completion update must also require an unclosed run as a final guard.

**Alternatives considered**:

- Check state without a lock: rejected because two requests could both complete and duplicate output.
- Rely only on an application-level in-memory lock: rejected because it is unsafe across instances and restarts.

## Decision: Validate the configured lifecycle transition as data

**Rationale**: The status transition seed is the source of permitted lifecycle paths. Completion resolves the `COMPLETED` status and confirms an active `IN_PROGRESS -> COMPLETED` row rather than relying only on hard-coded status names.

**Alternatives considered**:

- Hard-code the transition in the service: rejected because it bypasses the configured lifecycle model.
