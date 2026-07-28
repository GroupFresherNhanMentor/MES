# Implementation Plan: Inventory Domain Search Criteria & PageResponse Refactoring

**Branch**: `20260728131955-domain-search-criteria` | **Date**: 2026-07-28 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/20260728131955-domain-search-criteria/spec.md`

## Summary

Implement a pure Java `BaseSearchCriteria` base class in `fpt.qn.mes.common.dto` holding `page` and `size` fields (without validation annotations). Domain Search Criteria objects (`StockBalanceSearchCriteria`, `StockLotSearchCriteria`, `StockMovementSearchCriteria`) in `domain/repository/` extend `BaseSearchCriteria`. Application DTO Request objects (`StockBalanceSearchRequest`, `StockLotSearchRequest`, `StockMovementSearchRequest`) in `application/dto/request/` extend `PageRequest` with Spring/Jakarta validation annotations. Refactor repository interfaces (`StockBalanceRepository`, `StockLotRepository`, `StockMovementRepository`) and persistence adapters (`StockBalancePersistenceAdapter`, `StockLotPersistenceAdapter`, `StockMovementPersistenceAdapter`) to return `PageResponse<T>` instead of `PaginationResult<T>`.

## Technical Context

**Language/Version**: Java 25 / Spring Boot 4.1.0

**Primary Dependencies**: Lombok (for `@Getter`, `@Setter`, `@SuperBuilder`, `@EqualsAndHashCode`), Jakarta Validation (for Application DTOs), jOOQ 3.21 (`DSLContext`)

**Storage**: PostgreSQL 18

**Testing**: JUnit 5, Mockito

**Target Platform**: JVM / Linux Server

**Project Type**: Clean Architecture Repository & Search Refactoring (`fpt.qn.mes.inventory`)

**Constraints**:
- `BaseSearchCriteria` (`fpt.qn.mes.common.dto.BaseSearchCriteria`) is a pure Java abstract base class providing `page` (default 0) and `size` (default 20) fields without any framework/validation annotations.
- Application search requests (`*SearchRequest`) extend `PageRequest` (`fpt.qn.mes.common.dto.request.PageRequest`) and live in `application/dto/request/` with Jakarta Bean Validation annotations.
- Domain search criteria (`*SearchCriteria`) live in `domain/repository/` extending `BaseSearchCriteria`. Zero framework or validation imports in domain criteria.
- Repository methods return `PageResponse<T>` (`fpt.qn.mes.common.dto.response.PageResponse`).
- Persistence adapters build dynamic jOOQ `where` conditions based on domain search criteria.

## Constitution Check

- **Clean Architecture**: 4-layer structure maintained. Application DTOs live in `application/dto/request/` extending `PageRequest`. Domain search criteria live in `domain/repository/` extending pure Java `BaseSearchCriteria` with zero framework dependencies. PASS.
- **jOOQ Only**: Dynamic SQL building using jOOQ `Condition` lists in persistence adapters. PASS.
- **MapStruct Only**: Application DTO mappers map entities to response DTOs using `PageResponse.map(mapper::toDto)`. PASS.
- **Mandatory Tests**: Unit and integration tests covering search criteria filters and `PageResponse` output. PASS.

## Project Structure

### Documentation (this feature)

```text
specs/20260728131955-domain-search-criteria/
├── plan.md              # Implementation plan
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── InventorySearchContract.md
└── checklists/
    └── requirements.md
```

### Source Code Layout

```text
be/src/main/java/fpt/qn/mes/
├── common/
│   └── dto/
│       └── BaseSearchCriteria.java        # Pure Java base class (page, size)
└── inventory/
    ├── domain/
    │   ├── repository/
    │   │   ├── StockBalanceSearchCriteria.java # Extends BaseSearchCriteria
    │   │   ├── StockLotSearchCriteria.java     # Extends BaseSearchCriteria
    │   │   ├── StockMovementSearchCriteria.java # Extends BaseSearchCriteria
    │   │   ├── StockBalanceRepository.java    # Returns PageResponse<StockBalance>
    │   │   ├── StockLotRepository.java        # Returns PageResponse<StockLot>
    │   │   └── StockMovementRepository.java   # Returns PageResponse<StockMovement>
    ├── application/
    │   ├── dto/
    │   │   └── request/
    │   │       ├── StockBalanceSearchRequest.java  # Extends PageRequest + @Valid
    │   │       ├── StockLotSearchRequest.java      # Extends PageRequest + @Valid
    │   │       └── StockMovementSearchRequest.java # Extends PageRequest + @Valid
    │   └── service/
    │       └── InventoryService.java
    └── infrastructure/
        └── persistence/
            ├── StockBalancePersistenceAdapter.java
            ├── StockLotPersistenceAdapter.java
            └── StockMovementPersistenceAdapter.java

be/src/test/java/fpt/qn/mes/inventory/
└── persistence/
    └── SearchCriteriaTest.java
```
