# Implementation Plan: Domain Entity Validation & Value Objects

**Branch**: `20260728104201-domain-validation` | **Date**: 2026-07-28 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/20260728104201-domain-validation/spec.md`

## Summary

Enhance the MES `inventory` domain layer (`fpt.qn.mes.inventory.domain.entities`) by introducing simple pure Java Value Objects (`StockStatus`, `LotType`, `MovementType`) holding `id`, `name`, and `description` fields without helper methods. Entity invariant validations (`quantity >= 0`, `deductQuantity`, `lotNumber` non-blank) are maintained directly inside domain entities (`StockBalance`, `StockLot`, `StockMovement`).

## Technical Context

**Language/Version**: Java 25 / Spring Boot 4.1.0

**Primary Dependencies**: Lombok (for `@Getter`, `@Builder`, `@FieldDefaults`)

**Storage**: PostgreSQL 18 (jOOQ `DSLContext` for table record retrieval)

**Testing**: JUnit 5 (`StockBalanceTest`, `StockLotTest`, `StockMovementTest`, `StockStatusTest`, `LotTypeTest`, `MovementTypeTest`)

**Target Platform**: JVM / Linux Server

**Project Type**: Clean Architecture MES Domain Layer Refactoring (`fpt.qn.mes.inventory`)

**Constraints**:
- Domain objects MUST remain pure Java (no Spring, jOOQ, or Jackson imports in `domain/entities/`).
- Standard Java exceptions (`IllegalArgumentException`, `IllegalStateException`) thrown from domain methods.
- **Simple Value Objects**: `StockStatus`, `LotType`, and `MovementType` are data containers (`id`, `name`, `description`) without custom helper functions.

## Constitution Check

- **Clean Architecture**: 4-layer structure maintained. Domain entities remain pure Java. PASS.
- **jOOQ Only**: Database mapping contained strictly inside `infrastructure/persistence/InventoryRecordMapper.java`. PASS.
- **MapStruct Only**: Application DTO mappers map domain entities to DTOs. PASS.
- **Mandatory Tests**: Pure domain unit tests with JUnit 5 covering all state validation and Value Objects. PASS.

## Project Structure

### Documentation (this feature)

```text
specs/20260728104201-domain-validation/
├── plan.md              # Implementation plan
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── InventoryDomainContract.md
└── checklists/
    └── requirements.md
```

### Source Code Layout

```text
be/src/main/java/fpt/qn/mes/inventory/
├── domain/
│   ├── entities/
│   │   ├── StockStatus.java         # Value Object (id, name, description)
│   │   ├── LotType.java             # Value Object (id, name, description)
│   │   ├── MovementType.java        # Value Object (id, name, description)
│   │   ├── StockBalance.java        # Entity with domain validation & deductQuantity
│   │   ├── StockLot.java            # Entity with factory validation
│   │   └── StockMovement.java       # Entity with transaction validation
│   └── repository/
│       ├── StockBalanceRepository.java
│       ├── StockLotRepository.java
│       └── StockMovementRepository.java
├── application/
│   └── service/
│       └── InventoryService.java
└── infrastructure/
    └── persistence/
        └── InventoryRecordMapper.java # Maps jOOQ records + joined status/type fields to domain VOs

be/src/test/java/fpt/qn/mes/inventory/
└── domain/
    ├── StockStatusTest.java
    ├── LotTypeTest.java
    ├── MovementTypeTest.java
    ├── StockBalanceTest.java
    ├── StockLotTest.java
    └── StockMovementTest.java
```
