# Implementation Plan: Inventory Reference Lookup Endpoints

**Feature Branch**: `006-inventory-lookup-endpoints`
**Feature Spec**: [`spec.md`](spec.md)
**Created**: 2026-07-30

## Technical Context

- **Stack**: Java 25, Spring Boot 4.1.0, jOOQ 3.21, PostgreSQL 18
- **Architecture**: Clean Architecture (`domain`, `application`, `infrastructure`, `presentation`)
- **Key Classes**:
  - `InventoryController.java` (`presentation/`)
  - `InventoryUseCase.java` (`application/port/in/`)
  - `InventoryService.java` (`application/service/`)
  - `LotTypeSummaryDto.java`, `StockStatusSummaryDto.java`, `MovementTypeSummaryDto.java` (`application/dto/response/`)
  - `LotTypeSearchCriteria.java`, `StockStatusSearchCriteria.java`, `MovementTypeSearchCriteria.java` (`domain/repository/criteria/`)
  - `LotTypeRepository.java`, `StockStatusRepository.java`, `MovementTypeRepository.java` (`domain/repository/`)

## Constitution Check

- [x] **I. Clean Architecture is Non-Negotiable**: Follows `presentation → application.port.in → application.service → domain.repository ← infrastructure.persistence`.
- [x] **II. jOOQ Only for Database Access**: Database queries use `DSLContext`.
- [x] **III. MapStruct Only for Mapping**: `InventoryDtoMapper` maps domain entities to summary DTOs.
- [x] **IV. Exceptions from Service Layer**: Throws `AppException` from service layer.
- [x] **V. Consistent API Responses**: Endpoints return `ResponseEntity<ApiResponse<List<T>>>` or `ResponseEntity<ApiResponse<PageResponse<T>>>`.
- [x] **VI. Tests Are Mandatory**: Includes unit test and controller slice test cases for all three endpoints.

## Design Artifacts

- **Research**: [`research.md`](research.md)
- **Data Model**: [`data-model.md`](data-model.md)
- **Contracts**: [`contracts/inventory-lookup-api.md`](contracts/inventory-lookup-api.md)
- **Quickstart Guide**: [`quickstart.md`](quickstart.md)

## Implementation Phases

### Phase 1: Foundational DTOs & Criteria Classes
- Create `LotTypeSummaryDto.java` in `inventory/application/dto/response/`
- Create `LotTypeSearchCriteria.java`, `StockStatusSearchCriteria.java`, `MovementTypeSearchCriteria.java` in `inventory/domain/repository/criteria/`
- Create search request DTOs in `inventory/application/dto/request/`
- Update `InventoryDtoMapper.java` with `LotTypeSummaryDto toSummary(LotType entity);`

### Phase 2: Domain Repository & Service Ports
- Update `LotTypeRepository`, `StockStatusRepository`, `MovementTypeRepository` extending `BaseDomainRepository` with `search` & `count`
- Implement `LotTypePersistenceAdapter`, `StockStatusPersistenceAdapter`, `MovementTypePersistenceAdapter` using jOOQ `DSLContext`
- Update `InventoryUseCase.java` and `InventoryService.java` with criteria search methods (`getLotTypes`, `getStockStatuses`, `getMovementTypes`)

### Phase 3: Controller Endpoints
- Implement `GET /api/lot-types`, `GET /api/stock-statuses`, `GET /api/movement-types` in `InventoryController.java` using search request parameters
- Permit public unauthenticated access in `SecurityConfig.java`

### Phase 4: Verification & Testing
- Unit & controller slice tests in `InventoryControllerTest.java` and `InventoryServiceTest.java`
