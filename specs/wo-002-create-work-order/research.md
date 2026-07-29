# Technical Research & Decisions: Create Work Order (`POST /api/v1/work-orders`)

## 1. Active BOM Lookup & Material Requirement Calculation

### Decision
`WorkOrderService` will invoke `BomRepository.findActiveByFinishedProductId(finishedProductId)` to locate the active BOM for the given finished product. If no active BOM is found (`Optional.empty()`), the service throws a `BomNotActiveException("No active BOM found for finished product: " + finishedProductId)`.

When an active BOM is retrieved:
1. Extract `activeBom.getId()` and set it as `workOrder.bomId`.
2. Fetch BOM items using `BomRepository.findItemsByBomId(activeBom.getId())`.
3. For each `BomItem`, calculate:
   $$\text{requiredQuantity} = \text{plannedQuantity} \times \text{quantityPerUnit} \times (1 + \text{scrapRate})$$
4. Build `WorkOrderMaterial` domain entity with `workOrderId`, `materialProductId`, `requiredQuantity`, `reservedQuantity = 0`, `consumedQuantity = 0` and save via `WorkOrderRepository.saveMaterial(material)`.

### Rationale
- Decouples BOM lookup while enforcing business integrity.
- Ensures all material requirements are calculated atomically within the single `@Transactional` method of `WorkOrderService.createWorkOrder`.

---

## 2. Role-Based Authorization

### Decision
Annotate `WorkOrderController.create(...)` with:
`@PreAuthorize("hasRole('PLANNER')")`

### Rationale
- Aligns strictly with SRS requirement FR-WO-001 (only `PLANNER` role can create Work Orders).
- Returns HTTP 403 Forbidden automatically when accessed by `ADMIN`, `OPERATOR`, `AUDITOR`, or unprivileged roles.

---

## 3. Initial Status Resolution

### Decision
`WorkOrderService` queries `work_order_statuses` table for status code `"PLANNED"` (or `"DRAFT"` if `"PLANNED"` is default). The status ID is assigned to `workOrderStatusId`.

### Rationale
- Maintains data-driven status architecture defined in DB schema.

---

## 4. Audit Log Integration

### Decision
Log the action `CREATE_WORK_ORDER` with current user ID (`createdBy`), Work Order ID, and code upon successful creation.

### Rationale
- Satisfies SRS audit logging requirement for operational traceability.
