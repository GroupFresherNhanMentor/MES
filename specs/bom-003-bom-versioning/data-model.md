# Data Model: Bill of Materials Versioning (BOM Versioning)

**Feature**: BOM Versioning (`bom-003-bom-versioning`)  
**Created**: 2026-07-28  

## Entities & Table Mappings

### 1. `boms` (Header)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PRIMARY KEY | Unique BOM identifier |
| `finished_product_id` | `UUID` | FOREIGN KEY (`products.id`), NOT NULL | Target product |
| `version` | `INT` | NOT NULL | Sequential version number (`max(version) + 1`) |
| `bom_status_id` | `UUID` | FOREIGN KEY (`bom_statuses.id`), NOT NULL | FK to `bom_statuses` (`DRAFT`) |
| `created_by` | `UUID` | FOREIGN KEY (`users.id`), NOT NULL | User ID of creator |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT `now()` | Creation timestamp |

### 2. `bom_items` (Deep-Copied Component Lines)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `UUID` | PRIMARY KEY | New unique BOM item identifier |
| `bom_id` | `UUID` | FOREIGN KEY (`boms.id`), NOT NULL | Reference to new draft BOM `id` |
| `material_product_id` | `UUID` | FOREIGN KEY (`products.id`), NOT NULL | Copied material product ID |
| `quantity_per_unit` | `NUMERIC(15,4)` | NOT NULL, `> 0` | Copied quantity per unit |
| `scrap_rate` | `NUMERIC(5,4)` | NOT NULL, DEFAULT `0` | Copied scrap rate |

---

## Entity Cloning Logic

```text
Source BOM (v1 - ACTIVE/INACTIVE)                  New Cloned BOM (v2 - DRAFT)
┌───────────────────────────────┐                  ┌───────────────────────────────┐
│ id: uuid-1                    │                  │ id: uuid-2 (NEW)              │
│ product_id: product-A         │  createNewVersion │ product_id: product-A         │
│ version: 1                    │ ───────────────► │ version: 2 (maxVersion + 1)   │
│ status: ACTIVE                │                  │ status: DRAFT                 │
└──────────────┬────────────────┘                  └──────────────┬────────────────┘
               │                                                  │
       items   │                                          items   │ (deep copied)
               ▼                                                  ▼
┌───────────────────────────────┐                  ┌───────────────────────────────┐
│ item-1: Fabric 2.5m (scrap 2%)│                  │ item-101: Fabric 2.5m (NEW)   │
│ item-2: Thread 5.0m (scrap 0%)│                  │ item-102: Thread 5.0m (NEW)   │
└───────────────────────────────┘                  └───────────────────────────────┘
```

---

## Item Modification Guard Matrix

| BOM Status | `POST /api/boms/{id}/items` | `DELETE /api/boms/{id}/items/{itemId}` | Error Code |
|---|---|---|---|
| `DRAFT` | ✅ ALLOWED | ✅ ALLOWED | — |
| `ACTIVE` | ❌ DENIED | ❌ DENIED | `InvalidBomStatusException` (400) |
| `INACTIVE` | ❌ DENIED | ❌ DENIED | `InvalidBomStatusException` (400) |
