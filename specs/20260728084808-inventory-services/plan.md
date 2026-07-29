# Implementation Plan: Inventory Application Services

**Branch**: `20260728084808-inventory-services` | **Date**: 2026-07-28 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/20260728084808-inventory-services/spec.md`

## Summary

Implement the core application service (`InventoryServiceImpl`) and underlying domain/infrastructure persistence layers for the MES Inventory module. This encompasses stock lot management, stock movement logging (receipts, issues, transfers, adjustments), and atomic stock balance aggregation with pessimistic locking and quantity validation. Per user directive, the HTTP presentation controllers are explicitly excluded from this implementation plan.

## Technical Context

**Language/Version**: Java 25 / Spring Boot 4.1.0

**Primary Dependencies**: Spring Boot 4.1.0 (Core, Validation), jOOQ 3.21, MapStruct, Lombok

**Storage**: PostgreSQL 18

**Testing**: JUnit 5, Mockito (`@ExtendWith(MockitoExtension.class)`), Spring Boot Integration Test (`AbstractIntegrationTest`), Concurrency Tests (`CountDownLatch` + `ExecutorService`)

**Target Platform**: JVM / Linux Server

**Project Type**: Clean Architecture Web Service Backend Module (`fpt.qn.mes.inventory`)

**Performance Goals**: Sub-50ms stock movement recording; 100% quantity balance integrity under concurrent transactions

**Constraints**: Clean Architecture 4 layers; jOOQ `DSLContext` for database access (no JPA/Hibernate); MapStruct for object mappers; service-layer exception throwing (`AppException`); presentation controller explicitly omitted.

**Scale/Scope**: Scope includes `domain/entities`, `domain/repository`, `application/port/in`, `application/service`, `application/dto`, `application/mapper`, `application/exception`, and `infrastructure/persistence`.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Clean Architecture**: 4-layer module structure enforced (`domain`, `application`, `infrastructure`, `presentation`). PASS.
- **jOOQ Only**: All repository implementations use jOOQ `DSLContext`. PASS.
- **MapStruct Only**: All DTO/Entity/Record mapping handled via MapStruct interfaces. PASS.
- **Service Exceptions**: Business validation errors throw `AppException` from `InventoryServiceImpl`. PASS.
- **Mandatory Tests**: Unit tests with Mockito, integration tests with PostgreSQL container, and concurrency tests included. PASS.

## Project Structure

### Documentation (this feature)

```text
specs/20260728084808-inventory-services/
├── plan.md              # Implementation plan
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── InventoryUseCaseContract.md
└── checklists/
    └── requirements.md
```

### Source Code Layout

```text
be/src/main/java/fpt/qn/mes/inventory/
├── domain/
│   ├── entities/
│   │   ├── StockBalance.java
│   │   ├── StockLot.java
│   │   └── StockMovement.java
│   └── repository/
│       ├── StockBalanceRepository.java
│       ├── StockLotRepository.java
│       └── StockMovementRepository.java
├── application/
│   ├── port/in/
│   │   └── InventoryUseCase.java
│   ├── service/
│   │   └── InventoryServiceImpl.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateMovementRequest.java
│   │   │   └── CreateStockLotRequest.java
│   │   └── response/
│   │       ├── StockBalanceDto.java
│   │       ├── StockLotDto.java
│   │       └── StockMovementDto.java
│   ├── mapper/
│   │   ├── StockBalanceDtoMapper.java
│   │   ├── StockLotDtoMapper.java
│   │   └── StockMovementDtoMapper.java
│   └── exception/
│       ├── InventoryNotFoundException.java
│       └── InsufficientStockException.java
└── infrastructure/
    └── persistence/
        ├── StockBalanceRepositoryImpl.java
        ├── StockLotRepositoryImpl.java
        └── StockMovementRepositoryImpl.java

be/src/test/java/fpt/qn/mes/inventory/
├── service/
│   └── InventoryServiceImplTest.java
└── integration/
    └── InventoryIntegrationTest.java
```

**Structure Decision**: Clean Architecture single module structure following standard project layout under `be/src/main/java/fpt/qn/mes/inventory/`. Controller classes in `presentation/` are intentionally omitted per request.

## Complexity Tracking

> No constitution violations. Structure strictly follows standard Clean Architecture patterns.
