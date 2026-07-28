# Research & Architectural Decisions: Inventory Domain Search Criteria & PageResponse Refactoring

**Feature Branch**: `20260728131955-domain-search-criteria`

## Decision 1: Switching Repository Return Types from `PaginationResult<T>` to `PageResponse<T>`

### Decision
Replace `PaginationResult<T>` with `PageResponse<T>` across all repository interface signatures (`StockBalanceRepository`, `StockLotRepository`, `StockMovementRepository`) and persistence adapters.

### Rationale
- Standardizes pagination return objects across the entire backend architecture.
- `PageResponse<T>` contains `totalElements`, `totalPages`, `pageNumber`, `pageSize`, and `items` along with a `.map(Function<T, U> mapper)` helper method, eliminating boilerplate total-page calculations in application services.

---

## Decision 2: Separate Application DTOs (`*SearchRequest`) and Domain Criteria (`*SearchCriteria`)

### Decision
Create application DTOs (`StockBalanceSearchRequest`, `StockLotSearchRequest`, `StockMovementSearchRequest`) under `fpt.qn.mes.inventory.application.dto.request` extending `fpt.qn.mes.common.dto.request.PageRequest` with Jakarta Bean Validation annotations.
Create domain criteria POJOs (`StockBalanceSearchCriteria`, `StockLotSearchCriteria`, `StockMovementSearchCriteria`) under `fpt.qn.mes.inventory.domain.repository` containing `int page`, `int size`, and search filter parameters without any framework or validation annotations.

### Rationale
- Enforces strict Clean Architecture: domain layer remains pure Java with zero framework/annotation dependencies.
- Application DTOs receive and validate request parameters from REST controllers.
- Domain criteria objects are passed to repository interfaces and infrastructure persistence adapters.

---

## Decision 3: Dynamic jOOQ Query Building Strategy

### Decision
Persistence adapters construct a `List<Condition>` from non-null fields in the search criteria object:

```java
List<Condition> conditions = new ArrayList<>();
if (criteria.getProductId() != null) conditions.add(STOCK_LOTS.PRODUCT_ID.eq(criteria.getProductId()));
if (criteria.getLotNumber() != null && !criteria.getLotNumber().isBlank()) {
    conditions.add(STOCK_LOTS.LOT_NUMBER.containsIgnoreCase(criteria.getLotNumber()));
}
```

### Rationale
Provides clean type-safe dynamic SQL query building with jOOQ without sql injection risks.
