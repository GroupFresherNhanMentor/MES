# Phase 0 Research Findings: Get BOM Statuses (Lookup)

## 1. Lookup Infrastructure Reuse

- **Decision**: Reuse `LookupRepository.findAll("bom_statuses")` from package `fpt.qn.mes.common.service`.
- **Rationale**: `LookupRepository` is already built using jOOQ `DSL.field` queries to dynamically fetch `id`, `name`, and `description` from any master lookup table. This avoids creating redundant custom repository methods for simple lookup tables.
- **Alternatives Considered**: Creating a dedicated `findStatuses()` method in `BomRepository`. Rejected to avoid code duplication across master lookup tables.

## 2. API Contract & Security

- **Decision**: Endpoint `GET /api/boms/statuses` annotated with `@PreAuthorize("isAuthenticated()")`.
- **Rationale**: Lookup data is non-sensitive and required by all user roles (`ADMIN`, `PLANNER`, `FACTORY_MANAGER`, `OPERATOR`, `QC_INSPECTOR`) to populate dropdown lists in UI views.
