# Phase 0 Research: Release and Cancel Work Orders

## Research Topic 1: Multi-Lot Stock Release Strategy

### Problem Statement
When a Work Order reserves materials across multiple stock lots (e.g. 50 units from Lot A, 150 units from Lot B), releasing materials must credit stock back to the exact lots and locations where they were reserved, rather than assuming a single lot or location.

### Decision
Query past `stock_movements` for the Work Order with `movement_type_id` corresponding to `RESERVE`:
```sql
SELECT product_id, lot_id, from_warehouse_id, from_location_id, quantity 
FROM stock_movements 
WHERE work_order_id = :workOrderId AND movement_type_id = :reserveMovementTypeId;
```
For each historical reserve record, execute a stock balance transfer from `RESERVED` status back to `AVAILABLE` status for that specific `warehouse_id`, `location_id`, `product_id`, and `lot_id`.

### Rationale
- Preserves 100% lot traceability as required by SRS section FR-RES-002.
- Ensures lot expiry dates and location balances remain accurate.

---

## Research Topic 2: Concurrency Control & Lock Granularity

### Problem Statement
Simultaneous attempts to cancel a Work Order or release materials could cause race conditions or duplicate stock release movements.

### Decision
Use Pessimistic Locking (`SELECT FOR UPDATE`) on target rows in `stock_balances` and `work_orders`:
```java
dslCtx.selectFrom(WORK_ORDERS)
      .where(WORK_ORDERS.ID.eq(workOrderId))
      .forUpdate()
      .fetchOptional();
```

### Rationale
- Prevents race conditions when multiple planners attempt to cancel or release materials concurrently.
- Guarantees strict transactional consistency without retries.

---

## Research Topic 3: Work Order Status Transition Rules

### Problem Statement
Which Work Order statuses permit material release and cancellation?

### Decision
- **Material Release (`POST /release-materials`)**: Allowed only when Work Order is in `PLANNED` or `READY_TO_PRODUCE`. Blocked with HTTP 400 if `IN_PROGRESS` or `COMPLETED`.
- **Cancellation (`POST /cancel`)**: Allowed when Work Order is in `DRAFT`, `PLANNED`, `READY_TO_PRODUCE`, or `MATERIAL_SHORTAGE`. Blocked with HTTP 400 if `IN_PROGRESS` or `COMPLETED`.
- When cancellation succeeds:
  1. Status is updated to `CANCELLED`.
  2. If any materials are reserved (`reservedQuantity > 0`), the release process is automatically triggered within the same database transaction.

---

## Research Topic 4: REST Endpoint Design & Response Format

### Decision
- `POST /api/work-orders/{id}/release-materials` -> Returns `ApiResponse<WorkOrderDto>`.
- `POST /api/work-orders/{id}/cancel` -> Returns `ApiResponse<WorkOrderDto>`.

### Response Payload Structure
Consistent with existing endpoints:
```json
{
  "code": 1000,
  "message": "Materials released successfully",
  "data": {
    "id": "019facbf-...",
    "code": "WO-20260730-001",
    "status": "PLANNED",
    "materials": [...]
  }
}
```
