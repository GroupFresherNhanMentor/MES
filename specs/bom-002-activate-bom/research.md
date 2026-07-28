# Research & Technical Decisions: Activate Bill of Materials (Activate BOM)

**Feature**: Activate BOM (`bom-002-activate-bom`)  
**Created**: 2026-07-28  

## 1. Single Active Version Atomicity

### Problem Statement
SRS FR-BOM-002 specifies that only one BOM version can be `ACTIVE` per product at any given time. Database constraints do not enforce this at the table schema level, so the service layer must ensure atomic state update.

### Decision
Execute status transition in a single Spring `@Transactional` method:
1. Lock or fetch target BOM by ID.
2. Verify target BOM is in `DRAFT` status and contains at least 1 item (`itemCount > 0`).
3. Bulk update any existing `ACTIVE` BOM for `finishedProductId` to `INACTIVE` status:
   ```sql
   UPDATE boms SET bom_status_id = :inactiveStatusId WHERE finished_product_id = :productId AND bom_status_id = :activeStatusId;
   ```
4. Update target BOM status to `ACTIVE`:
   ```sql
   UPDATE boms SET bom_status_id = :activeStatusId WHERE id = :bomId;
   ```
5. Return updated `BomDto`.

### Rationale
Executing both updates in one transaction guarantees atomicity without race conditions or orphan active versions.

### Alternatives Considered
- *Database partial unique index (`CREATE UNIQUE INDEX ON boms (finished_product_id) WHERE status = 'ACTIVE'`) Cal*: Would enforce single active at DB level, but Flyway schema changes require team migration approval. Using transaction handling in repository adapter meets requirements immediately and cleanly.

---

## 2. Validation of Empty BOM Activation

### Problem Statement
Attempting to activate a BOM without items must be blocked to prevent invalid production planning.

### Decision
`BomRepository` will provide `countItemsByBomId(UUID bomId)` or check `bom.getItems().isEmpty()`. If item count is 0, ném `EmptyBomException` (HTTP 400 Bad Request).

### Rationale
Clear domain exception with explicit error code (`BAD_REQUEST`) provides actionable feedback to API callers.

---

## 3. Status Transition Rules

### Problem Statement
Only `DRAFT` BOMs can be activated. Activation requests for `ACTIVE` or `INACTIVE` BOMs must fail.

### Decision
Validate current status before transition:
- Current Status == `ACTIVE` → Throw `InvalidBomStatusException` ("BOM is already active").
- Current Status == `INACTIVE` → Throw `InvalidBomStatusException` ("Cannot activate historical INACTIVE BOM; create a new version").
- Current Status == `DRAFT` → Proceed to activate.
