# Data Model: Create Bill of Materials Header

**Feature**: `bom-001-create-header`  
**Date**: 2026-07-28  
**Spec**: [spec.md](spec.md)

## Entities & Data Model

### 1. `Bom` Domain Entity (`fpt.qn.mes.bom.domain.entities.Bom`)

Pure Java domain entity with no framework imports.

```java
public class Bom {
    UUID id;
    UUID finishedProductId;
    Integer version;
    UUID bomStatusId;
    UUID createdBy;
    Instant createdAt;
    List<BomItem> items;
}
```

#### Field Rules & Constraints:
- `id`: `UUID` (Generated via `UUID.randomUUID()` in domain factory method `Bom.create(...)`).
- `finishedProductId`: `UUID` (NOT NULL, must refer to an active product with type `FINISHED_GOOD` or `SEMI_FINISHED`).
- `version`: `Integer` (NOT NULL, MUST be > 0).
- `bomStatusId`: `UUID` (NOT NULL, refers to `DRAFT` status in `bom_statuses`).
- `createdBy`: `UUID` (Refers to user id from `CurrentUserPort`).
- `createdAt`: `Instant` (NOT NULL, defaults to current time).

---

### 2. Database Schema (`boms` table)

Flyway Table Schema: `boms` (already present in `V20260726194837__init.sql`):

```sql
CREATE TABLE boms (
    id                      UUID PRIMARY KEY,
    finished_product_id     UUID NOT NULL REFERENCES products(id),
    version                 INTEGER NOT NULL,
    bom_status_id           UUID NOT NULL REFERENCES bom_statuses(id),
    created_by              UUID REFERENCES users(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_boms_product_version UNIQUE (finished_product_id, version)
);
```

---

### 3. Status Lookup Table (`bom_statuses`)

Pre-populated lookup values in PostgreSQL:
- `DRAFT`: Initial status when BOM header is created.
- `ACTIVE`: Active production BOM formula.
- `INACTIVE`: Retired or replaced BOM formula version.

---

### 4. Validation Rules & Mappings

| Request Field | Target Field | Validation Rules | Error Code / Status |
|---|---|---|---|
| `finishedProductId` | `finished_product_id` | Must not be null; must exist; type must be `FINISHED_GOOD` or `SEMI_FINISHED` | `INVALID_INPUT` (400) / `NOT_FOUND` (404) |
| `version` | `version` | Must not be null; must be >= 1; `(finished_product_id, version)` combination must be unique | `INVALID_INPUT` (400) / `CONFLICT` (409) |
| N/A (Server set) | `bom_status_id` | Set to `DRAFT` status UUID | N/A |
| N/A (Server set) | `created_by` | Extracted from `CurrentUserPort` | `UNAUTHORIZED` (401) |
