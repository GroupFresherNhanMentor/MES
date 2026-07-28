# Implementation Plan: Activate Bill of Materials (Activate BOM)

**Branch**: `feature/bom-002-activate-bom` | **Date**: 2026-07-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/bom-002-activate-bom/spec.md` and SRS §3.5 FR-BOM-002

## Summary

Implement the backend business logic and API endpoint (`POST /api/boms/{id}/activate`) allowing Planners to activate a draft BOM. Activation enforces key business constraints:
1. Target BOM must be in `DRAFT` status.
2. Target BOM must have at least 1 component item (cannot activate empty BOM).
3. Any existing `ACTIVE` BOM for the same product is automatically set to `INACTIVE`.
4. Target BOM transitions from `DRAFT` to `ACTIVE`.
All updates execute atomically within a single transactional boundary (`@Transactional`).

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21, MapStruct 1.6.3, Lombok, Jakarta Validation  
**Storage**: PostgreSQL 18 (Tables `boms`, `bom_items`, `bom_statuses`, `products`)  
**Testing**: JUnit 5, Mockito, Spring Boot Test (`@ExtendWith(MockitoExtension.class)`, `AbstractIntegrationTest`)  
**Target Platform**: Java Virtual Machine / Docker  
**Project Type**: REST Web Service (Spring MVC WebMVC)  
**Performance Goals**: Response time < 500ms for activation endpoint  
**Constraints**: Clean Architecture 4-layer isolation, jOOQ only (no JPA/Hibernate), MapStruct for DTO mapping, no custom SQL strings  
**Scale/Scope**: Manufacturing Execution System (MES) BOM module  

## Constitution Check

*GATE: All items must pass.*

- [x] **I. Clean Architecture**: Follows exact 4-layer structure (`fpt.qn.mes.bom`).
- [x] **II. jOOQ Only**: Uses jOOQ `DSLContext` for atomic status updates.
- [x] **III. MapStruct Only for DTOs**: `BomDtoMapper` uses MapStruct for entity ↔ DTO mapping.
- [x] **IV. Exceptions from Service Layer**: Throws `BomNotFoundException`, `InvalidBomStatusException`, `EmptyBomException` extending `AppException`.
- [x] **V. Consistent API Responses**: `BomController` returns `ResponseEntity<ApiResponse<BomDto>>`.
- [x] **VI. Mandatory Testing**: Unit tests (`BomServiceTest`) + Integration tests (`BomIntegrationTest`).

## Project Structure

### Documentation (this feature)

```text
specs/bom-002-activate-bom/
├── plan.md                # This file
├── research.md            # Technical research & decisions
├── data-model.md          # Entity definitions & schema mappings
├── quickstart.md          # API testing & validation scenarios
└── contracts/
    └── activate-bom-api.json # OpenAPI spec contract
```

### Source Code Layout

```text
be/src/main/java/fpt/qn/mes/bom/
├── domain/
│   ├── entities/
│   │   └── Bom.java            # Domain logic for status transition
│   └── repository/
│       └── BomRepository.java # Persistence contract methods
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── BomUseCase.java# Use case interface with activateBom
│   ├── service/
│   │   └── BomService.java    # Service implementing transactional activation
│   ├── dto/
│   │   └── response/
│   │       └── BomDto.java
│   └── exception/
│       ├── EmptyBomException.java
│       └── InvalidBomStatusException.java
├── infrastructure/
│   └── persistence/
│       └── BomPersistenceAdapter.java # jOOQ persistence queries
└── presentation/
    └── BomController.java     # POST /api/boms/{id}/activate endpoint
```

**Structure Decision**: Standard 4-layer Clean Architecture as specified in `docs/rules/backend-architecture.md`.

## Complexity Tracking

*No constitution violations.*
