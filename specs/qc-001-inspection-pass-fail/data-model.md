# Data Model: QC Inspection — Pass / Fail

---

## Entities

### QC Status (Lookup table — `qc_statuses`)

| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK |
| name | VARCHAR(50) | NOT NULL, UNIQUE |
| description | VARCHAR(255) | |

**Seed data**: PENDING_INSPECTION, PASSED, FAILED, ON_HOLD, REWORK_REQUIRED, SCRAPPED

---

### QC Action (Lookup table — `qc_actions`)

| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK |
| name | VARCHAR(50) | NOT NULL, UNIQUE |
| description | VARCHAR(255) | |

**Seed data**: HOLD, REWORK, SCRAP

---

### Defect Type (Lookup table — `defect_types`)

| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK |
| code | VARCHAR(50) | NOT NULL, UNIQUE |
| name | VARCHAR(255) | NOT NULL |
| description | VARCHAR(255) | |

**Seed data**: SCRATCH, DIMENSION_ERROR, WEIGHT_ERROR, COLOR_DEFECT, CRACK, CONTAMINATION, FUNCTIONAL_FAIL, ASSEMBLY_ERROR, LABEL_ERROR, OTHER

---

### Quality Inspection (`quality_inspections`)

| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK |
| work_order_id | UUID | NOT NULL, FK → work_orders(id) |
| product_id | UUID | NOT NULL, FK → products(id) |
| lot_id | UUID | NOT NULL, FK → stock_lots(id) |
| quantity | NUMERIC(18,4) | NOT NULL, CHECK (> 0) |
| qc_status_id | UUID | NOT NULL, FK → qc_statuses(id) |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() |

**Derived field (service layer)**:
- `remainingQuantity` = `quantity - SUM(quality_inspection_results.quantity)`

**Status transitions**:

```
PENDING_INSPECTION ── pass() ──→ PASSED (terminal)
                  ── fail(SCRAP) ──→ FAILED (terminal)
                  ── fail(HOLD)  ──→ ON_HOLD (terminal)
                  ── fail(REWORK) ──→ REWORK_REQUIRED (terminal)
```

> **Terminal**: In this feature, all end states are terminal. Downstream processing will be added in future features.

---

### Quality Inspection Result (`quality_inspection_results`)

| Field | Type | Constraints |
|-------|------|-------------|
| id | UUID | PK |
| inspection_id | UUID | NOT NULL, FK → quality_inspections(id) ON DELETE CASCADE |
| is_pass | BOOLEAN | NOT NULL |
| quantity | NUMERIC(18,4) | NOT NULL, CHECK (> 0) |
| defect_type_id | UUID | FK → defect_types(id) — nullable if pass |
| reason | VARCHAR(500) | Nullable if pass |
| action_id | UUID | FK → qc_actions(id) — nullable if pass |
| inspector_id | UUID | NOT NULL, FK → users(id) |
| inspected_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() |
| note | VARCHAR(500) | |

**Constraint**: `CHECK(is_pass = TRUE OR (defect_type_id IS NOT NULL AND reason IS NOT NULL))`

---

### Stock Movement (existing — `stock_movements`)

Used by QC to record inventory transitions.

| Movement type | From | To |
|--------------|------|----|
| QC_RELEASE | QUALITY_INSPECTION | AVAILABLE |
| QC_HOLD | QUALITY_INSPECTION | ON_HOLD |
| SCRAP | QUALITY_INSPECTION | SCRAPPED |

---

## Relationships

```
quality_inspections
  │  └── work_order_id ──→ work_orders(id)
  │  └── product_id ──→ products(id)
  │  └── lot_id ──→ stock_lots(id)
  │  └── qc_status_id ──→ qc_statuses(id)
  │
  └── quality_inspection_results
        │  └── inspection_id ──→ quality_inspections(id)
        │  └── defect_type_id ──→ defect_types(id)
        │  └── inspector_id ──→ users(id)
        │  └── action_id ──→ qc_actions(id)
```

---

## Validation Rules

| Rule | FR | Enforced at |
|------|----|-------------|
| passedQuantity > 0 | FR-003 | Service |
| failedQuantity > 0 | FR-004 | Service |
| pass/fail quantity ≤ remainingQuantity | FR-006 | Service |
| fail requires defectTypeId + reason | FR-005 | DB CHECK + Service |
| invalid actionId → reject | FR-004 | Service (FK ensures valid IDs) |
| inspection already fully processed → reject | FR-003/FR-004 | Service |
| concurrent pass/fail on same inspection | — | Optimistic locking via version or application-level check |