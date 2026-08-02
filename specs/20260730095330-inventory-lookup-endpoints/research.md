# Research: Inventory Reference Lookup Endpoints

## Decision 1: DTO Return Types for Reference Lookup Endpoints
- **Decision**: Use `LotTypeSummaryDto`, `StockStatusSummaryDto`, and `MovementTypeSummaryDto` for the response lists returned by `GET /api/lot-types`, `GET /api/stock-statuses`, and `GET /api/movement-types`.
- **Rationale**: Reuses existing summary DTOs in `inventory/application/dto/response/` (`StockStatusSummaryDto`, `MovementTypeSummaryDto`) and creates `LotTypeSummaryDto` matching the exact same pattern (`id`, `name`, `description`).
- **Alternatives Considered**: Returning raw `Map<String, Object>` or full domain entities (rejected per Constitution I & III and API consistency rules).

## Decision 2: Persistence Approach for Reference Tables
- **Decision**: Use `BaseDomainRepository` criteria search pattern with `search(criteria)` and `count(criteria)` using custom search criteria classes (`LotTypeSearchCriteria`, `StockStatusSearchCriteria`, `MovementTypeSearchCriteria`). Do **NOT** use `findAll()`.
- **Rationale**: User explicit instruction to use criteria search instead of `findAll()` to align with project-wide repository conventions (`StockLotRepository`, `StockAdjustmentApprovalRepository`).
- **Alternatives Considered**: `findAll()` unpaginated query (rejected per user explicit rule).

## Decision 3: Security Authorization Rules
- **Decision**: Include `/api/lot-types`, `/api/stock-statuses`, `/api/movement-types` in `SecurityConfig.java` `permitAll()` list.
- **Rationale**: Reference lookup data is public data needed for populating frontend dropdowns without requiring a JWT token.
