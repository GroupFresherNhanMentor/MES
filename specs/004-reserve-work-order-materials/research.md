# Research: Reserve Work Order Materials

## Decision 1: Use a dedicated Work Order reservation use case

**Decision**: Add a `reserveMaterials` operation to the Work Order input port and implement the orchestration in the Work Order application service. Keep stock allocation behind Work Order-owned output/repository ports implemented with jOOQ.

**Rationale**:

- Reservation changes Work Order status, Work Order material quantities, stock balances, stock movements, and audit records in one business transaction.
- The architecture allows Work Order to depend on master and BOM use-case interfaces, but does not allow it to import Inventory repositories or persistence adapters.
- A dedicated operation avoids coupling the Work Order flow to the generic `InventoryService.recordMovement`, which currently updates only one balance and does not implement a two-status transfer.

**Alternatives considered**:

- Put the operation in `InventoryService`: rejected because Inventory is upstream of Work Order in the module dependency graph and would create a forbidden dependency or circular workflow.
- Reuse `InventoryService.recordMovement`: rejected because it does not decrement `AVAILABLE` and increment `RESERVED` atomically for a reservation.
- Add business logic to the controller: rejected by the Clean Architecture and service-layer exception rules.

## Decision 2: Resolve the source warehouse by configured code

**Decision**: Resolve the warehouse with code `RAW_MATERIAL_WAREHOUSE` on the server. Do not accept `sourceWarehouseId` in the request. Add a warehouse lookup by code through the master application boundary and a configurable default code with `RAW_MATERIAL_WAREHOUSE` as its default value.

**Rationale**:

- The caller must not choose a warehouse outside the designated raw-material source.
- The `warehouses.code` column is unique, so a code lookup identifies one warehouse.
- Existing Warehouse interfaces expose only ID lookup and code existence, so a semantic lookup operation is required.

**Alternatives considered**:

- Accept `sourceWarehouseId` from the client: rejected because it could bypass the designated source warehouse.
- Search every warehouse and aggregate stock: rejected by the feature rule.
- Hard-code a warehouse UUID: rejected because IDs are application-generated and environments differ.

## Decision 3: Use pessimistic row locking with deterministic FIFO allocation

**Decision**: Lock the Work Order row and all eligible `AVAILABLE` stock balance rows using `SELECT FOR UPDATE`. Select lots in FIFO order by `stock_lots.created_at`, with deterministic tie-breakers for equal timestamps. Machine availability is locked and checked only at start.

**Rationale**:

- The feature explicitly requires pessimistic locking.
- Locking the Work Order prevents duplicate reservations for one order.
- Locking all candidate balances before applying changes allows a complete availability check and prevents negative stock during concurrent reservations.
- The existing balance lock omits `stock_status_id`; the reservation query must include it so `AVAILABLE` and `RESERVED` rows are distinct.

**Alternatives considered**:

- Optimistic locking through `stock_balances.version`: rejected for this feature because pessimistic locking was explicitly selected.
- Lock only the first matching lot: rejected because a material may span several lots and all allocation rows must be checked consistently.
- Lock rows in arbitrary order: rejected because competing requests could deadlock; use a deterministic product/lot/location order.

## Decision 4: Make reservation all-or-nothing and persist shortage status

**Decision**: Validate every material before changing stock. On shortage, write only the `MATERIAL_SHORTAGE` status and its audit record, then return a structured `INSUFFICIENT_STOCK` error without stock or material reservation changes. Configure the service transaction so the expected shortage exception does not roll back the shortage status record.

**Rationale**:

- The business requirement explicitly forbids partial reservation.
- A shortage status must remain visible so the Planner can replenish stock and retry.
- The normal `RuntimeException` rollback behavior would otherwise undo the shortage status together with the failed reservation.

**Alternatives considered**:

- Throw the shortage exception before writing the status: rejected because the Work Order would remain `PLANNED` and lose the shortage signal.
- Mutate stock and compensate after failure: rejected because it creates unnecessary intermediate state and weakens atomicity.
- Return a normal success response for shortage: rejected because the API contract requires `400 INSUFFICIENT_STOCK`.

## Decision 5: Add reservation-specific error details and movement traceability

**Decision**: Add `INSUFFICIENT_STOCK` to the common error-code model, extend the application error path to carry structured shortage details, and extend the stock movement domain/mapping path with `workOrderId`.

**Rationale**:

- Existing `InsufficientStockException` maps to generic `INVALID_INPUT` and cannot carry the required missing-material list.
- The database already has `stock_movements.work_order_id`, but the domain and record mapper currently omit it.
- Each reservation movement must be traceable to its Work Order and selected lot.

**Alternatives considered**:

- Put shortage details only in the message string: rejected because clients need reliable structured data.
- Add a second reservation ledger table: rejected because the existing movement ledger already models this event.
- Leave `workOrderId` unmapped: rejected because it violates auditability and the data model.

## Decision 6: Standardize the Work Order route

**Decision**: Expose Work Order operations only under `/api/work-orders`, including `/api/work-orders/{id}/reserve-materials`.

**Rationale**:

- The project-wide API convention uses unversioned `/api/{resources}` paths.
- A single controller root keeps all Work Order operations and authorization rules consistent.
- Tests, contracts, and client references use the same canonical route.

**Alternatives considered**:

- Retain a versioned route alias: rejected because no shipped compatibility requirement exists and duplicate routes increase maintenance cost.
- Version only the reserve action: rejected because one resource must not expose mixed route conventions.
- Add a second duplicate controller with copied business logic: rejected because it creates two presentation paths for one use case.

## Decision 7: Audit status transitions in the same business transaction

**Decision**: Add a focused audit output port and jOOQ adapter that inserts `RESERVE_MATERIAL` records with actor, Work Order entity, old status, new status, and timestamp. The success and shortage status writes use the same transaction boundary as reservation orchestration.

**Rationale**:

- The `audit_logs` table already supports the required fields.
- No Java audit module currently exists, so a focused output port avoids coupling the Work Order application service to a missing module.
- Same-transaction audit writes prevent a committed status change without its audit record.

**Alternatives considered**:

- Log only from the controller: rejected because controller logging would not cover service-triggered transitions and could run outside the transaction.
- Add audit logging after commit: rejected because a failure could leave an unaudited status transition.
- Modify the existing stock movement only: rejected because movement and status-transition audit have different purposes.
