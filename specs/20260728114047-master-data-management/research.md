# Research: Master Data Management

## Resolved Unknowns

### 1. Machine Status Values & Canonical Names

**Issue (FR-017)**: The spec listed status names as AVAILABLE, RUNNING, DOWN, UNDER_MAINTENANCE, RETIRED — but the existing seed file (`be/src/main/resources/seed/machine-statuses.json`) and DB schema define different names.

**Decision**: Adopt the seed file names as canonical. These are already in production seed data.

| Canonical Name | Spec Name (superseded) | Description |
|---------------|----------------------|-------------|
| AVAILABLE | AVAILABLE | Machine is ready for production |
| IN_USE | RUNNING | Machine is currently running production |
| UNDER_MAINTENANCE | UNDER_MAINTENANCE | Machine is being serviced |
| BROKEN | DOWN | Machine has a fault |
| INACTIVE | RETIRED | Machine is decommissioned |

**Rationale**: Seed data is the source of truth per tech stack rules. Changing seed names would require a new migration to rename existing data. Acceptance scenarios in the spec will be adjusted to use the canonical names.

---

### 2. Machine Status Transition Matrix

**Issue (FR-017)**: The spec required a defined transition matrix for machine statuses. No matrix was provided.

**Decision**: The following transitions are valid:

```
                  ┌─────────────┐
                  │  INACTIVE   │  (terminal — no outgoing)
                  └─────────────┘
                       ↑
         ┌─────────────┤
         │             │
         │    ┌───────────────────┐
         │    │    BROKEN         │
         │    └───────────────────┘
         │             ↑
         │             │
┌──────────────┐   ┌───────────────────┐
│  AVAILABLE   │←──│ UNDER_MAINTENANCE │
└──────────────┘   └───────────────────┘
       ↑                    ↑
       │                    │
       │    ┌─────────────┐ │
       └────│   IN_USE    │─┘
            └─────────────┘
```

| From ↓ / To → | AVAILABLE | IN_USE | UNDER_MAINTENANCE | BROKEN | INACTIVE |
|---------------|-----------|--------|-------------------|--------|----------|
| AVAILABLE | — | ✅ Start production | ✅ Preventive maint | ❌ | ✅ Decommission |
| IN_USE | ✅ Stop production | — | ✅ Maint after run | ✅ Fault reported | ❌ |
| UNDER_MAINTENANCE | ✅ Maint complete | ❌ | — | ❌ | ✅ Decommission |
| BROKEN | ❌ | ❌ | ✅ Repair started | — | ✅ Beyond repair |
| INACTIVE | ❌ | ❌ | ❌ | ❌ | — |

**Rationale**: Based on standard manufacturing equipment lifecycle patterns. INACTIVE is a terminal state (no outgoing transitions) to prevent reactivation of decommissioned equipment. BROKEN can only be repaired (→ UNDER_MAINTENANCE) or scrapped (→ INACTIVE).

---

### 3. Product Types — System-Fixed vs Admin-Configurable

**Issue (Assumptions)**: Spec asked "should Admin be able to add/modify product types, or are they fixed enums?"

**Decision**: Product types are **system-defined seeds** loaded from `be/src/main/resources/seed/product-types.json`. Admin cannot modify them at runtime.

**Rationale**:
- The existing seed file contains the 5 types (RAW_MATERIAL, SEMI_FINISHED, FINISHED_GOOD, CONSUMABLE, SPARE_PART).
- These types are foundational to the factory's product classification and changing them risks breaking dependent modules (BOM, Inventory, Work Order).
- The tech stack rules specify lookup data is seeded via JSON files — no CRUD endpoints for product types.

---

### 4. Status Lookup Tables — All System-Fixed

Consistent with product types, the following lookup tables are also system-fixed seeds (loaded from existing JSON files, no CRUD):

| Lookup Table | Seed File | Values |
|-------------|-----------|--------|
| product_types | seed/product-types.json | RAW_MATERIAL, SEMI_FINISHED, FINISHED_GOOD, CONSUMABLE, SPARE_PART |
| product_statuses | seed/product-statuses.json | ACTIVE, INACTIVE |
| units_of_measure | seed/units-of-measure.json | PCS, KG, G, L, ML, M, M2, M3, BOX, SET, ROLL, TON |
| warehouse_statuses | seed/warehouse-statuses.json | ACTIVE, INACTIVE |
| location_statuses | seed/location-statuses.json | ACTIVE, INACTIVE |
| line_statuses | seed/line-statuses.json | ACTIVE, INACTIVE, MAINTENANCE |
| machine_statuses | seed/machine-statuses.json | AVAILABLE, IN_USE, UNDER_MAINTENANCE, BROKEN, INACTIVE |

All values can be extended via new seed JSON entries in a future release — no code changes needed, only data.

---

### 5. Unit Field on Product

**Issue**: The spec mentions `unit` as a free-text or predefined field on Product.

**Decision**: The DB schema (existing) uses `unit_id UUID REFERENCES units_of_measure(id)` — a FK to the units_of_measure lookup table. The `unit` field on Product domain entity stores the UUID reference, not a string. DTOs will expose the unit name/description for convenience.

**Rationale**: Matches existing schema. Prevents free-text inconsistencies.

---

### 6. Seed Data Loading

The `DataSeeder` (`user/infrastructure/seed/DataSeeder.java`) is a stub (`ApplicationRunner` with empty `run()`). A seed loading utility will be created as part of `common` or a shared infrastructure component to load all JSON seed files into their corresponding lookup tables on application startup. This utility should:
- Read each JSON seed file from `classpath:seed/*.json`
- Check if each record already exists (by name) — upsert pattern
- Run only on first startup or when tables are empty
