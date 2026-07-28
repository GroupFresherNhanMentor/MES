# Data Model: Activate Bill of Materials (Activate BOM)

**Feature**: Activate BOM (`bom-002-activate-bom`)  
**Created**: 2026-07-28  

## Entities & Table Mappings

### 1. `boms` (Header)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PRIMARY KEY | Unique BOM identifier |
| `finished_product_id` | `UUID` | FOREIGN KEY (`products.id`), NOT NULL | Target product being manufactured |
| `version` | `INT` | NOT NULL | BOM version number (unique per product) |
| `bom_status_id` | `UUID` | FOREIGN KEY (`bom_statuses.id`), NOT NULL | FK to `bom_statuses` (`DRAFT`, `ACTIVE`, `INACTIVE`) |
| `created_by` | `UUID` | FOREIGN KEY (`users.id`), NOT NULL | User ID of creator |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `now()` | Timestamp of creation |

### 2. `bom_statuses` (Lookup)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PRIMARY KEY | Status identifier |
| `name` | `VARCHAR(50)` | NOT NULL, UNIQUE | Status name (`DRAFT`, `ACTIVE`, `INACTIVE`) |

### 3. `bom_items` (Component Lines)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PRIMARY KEY | BOM item identifier |
| `bom_id` | `UUID` | FOREIGN KEY (`boms.id`), NOT NULL | Parent BOM reference |
| `material_product_id` | `UUID` | FOREIGN KEY (`products.id`), NOT NULL | Material/component product ID |
| `quantity_per_unit` | `NUMERIC(15,4)` | NOT NULL, `> 0` | Quantity required per 1 unit of output |
| `scrap_rate` | `NUMERIC(5,4)` | NOT NULL, DEFAULT `0` | Expected scrap percentage |

---

## State Transition Diagram

```
[ DRAFT ] ─── activateBom() ───► [ ACTIVE ]
 (with items > 0)                  │
                                   │ (when new version is activated)
                                   ▼
                              [ INACTIVE ]
```

### Transition Validation Matrix

| Initial State | Target Action | Allowed? | Result State | Error if Denied |
|---|---|---|---|---|
| `DRAFT` (items > 0) | `activateBom()` | ✅ YES | `ACTIVE` | — |
| `DRAFT` (items = 0) | `activateBom()` | ❌ NO | `DRAFT` | `EmptyBomException` (400) |
| `ACTIVE` | `activateBom()` | ❌ NO | `ACTIVE` | `InvalidBomStatusException` (400) |
| `INACTIVE` | `activateBom()` | ❌ NO | `INACTIVE` | `InvalidBomStatusException` (400) |
