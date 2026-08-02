# Research: Update Work Order

## Decision 1: Status Lookup Mechanism

**Decision**: Query `work_order_statuses` table by UUID to resolve status name. Use the name to enforce the `DRAFT ⇄ PLANNED` guard in the service layer.

**Rationale**: The `work_order_statuses` table uses UUID primary keys with a `name` column (VARCHAR(50), unique). Since the `UpdateWorkOrderRequest` carries a `workOrderStatusId` (UUID), the service must look up the corresponding status name to validate whether the transition is allowed. A new repository method `findStatusNameById(UUID)` on `WorkOrderRepository` (implemented via jOOQ `WORK_ORDER_STATUSES` table) is the cleanest approach, consistent with the existing data-driven transition design.

**Alternatives considered**:
- Hardcode DRAFT/PLANNED UUIDs as constants → rejected because UUIDs are generated at seed time and vary across environments.
- Use `work_order_status_transitions` table for validation → rejected because PUT only handles `DRAFT ⇄ PLANNED` (a subset of all transitions); the full transition table is for action endpoints.

---

## Decision 2: Seed Data for `PLANNED → DRAFT` Transition

**Decision**: Add a reverse transition `PLANNED → DRAFT` to `work-order-status-transitions.json` seed data.

**Rationale**: The current seed data only has `DRAFT → PLANNED` but the spec requires bidirectional transitions (`DRAFT ⇄ PLANNED`). Without this seed entry, the data-driven transition validation would reject `PLANNED → DRAFT`. However, since the PUT endpoint hardcodes its own guard (only DRAFT/PLANNED allowed), the seed data change is for consistency and future-proofing.

**Alternatives considered**:
- Skip seed data update and only enforce in code → rejected because it creates inconsistency between the data-driven transition model and the actual API behavior.

---

## Decision 3: Material Recalculation Strategy

**Decision**: When `plannedQuantity` changes, fetch the BOM items via the existing `bomRepository.findActiveByFinishedProductId()` (using the WO's `finishedProductId`), then recalculate and update each `work_order_materials` row in-place.

**Rationale**: The existing `createWorkOrder` method already uses this exact pattern (fetch BOM items, calculate `requiredQuantity = plannedQuantity × quantityPerUnit × (1 + scrapRate)`). For update, we reuse the same formula but update existing material rows rather than inserting new ones. A new repository method `updateMaterial(WorkOrderMaterial)` is needed.

**Alternatives considered**:
- Delete all materials and re-insert → rejected because it loses `reservedQuantity` and `consumedQuantity` values that may have been set by other operations.
- Only update `required_quantity` column via SQL → acceptable but breaks the Clean Architecture pattern; the service layer should handle the logic.

---

## Decision 4: Partial Update (Null-Safe Merge)

**Decision**: Treat `null` fields in `UpdateWorkOrderRequest` as "keep existing value". Only non-null fields are applied to the Work Order.

**Rationale**: The spec explicitly states "if a field is null in the request, the existing value is preserved" (partial update). This is the standard PATCH-like behavior applied to a PUT endpoint. The service layer merges the request fields with the existing entity before saving.

**Alternatives considered**:
- Require all fields in PUT (full replace) → rejected because the user explicitly requested partial update support.

---

## Decision 5: Unique Code Check Exclusion

**Decision**: When checking code uniqueness, exclude the current Work Order's own ID from the duplicate check.

**Rationale**: If a Planner sends a PUT request keeping the same code unchanged, the uniqueness check must not flag it as a conflict. The repository query should be: `SELECT COUNT(*) FROM work_orders WHERE code = ? AND id != ?`.

**Alternatives considered**:
- Skip uniqueness check if code hasn't changed → requires comparing old vs new code, adds complexity. The exclusion-by-ID approach is simpler and handles all cases.

---

## Decision 6: New Exception Class

**Decision**: Create `InvalidWorkOrderStateException` extending `AppException` with HTTP 400 and error code `BAD_REQUEST`.

**Rationale**: The spec requires rejecting updates when the Work Order is not in DRAFT/PLANNED status, and rejecting invalid status transitions. A dedicated exception class follows the project pattern (each module has its own exceptions in `application/exception/`).

**Alternatives considered**:
- Reuse `AppException` directly → rejected because the project constitution mandates module-specific exception classes.
- Create separate exceptions for each validation → rejected to avoid exception class explosion; one class covers all state-related rejections.
