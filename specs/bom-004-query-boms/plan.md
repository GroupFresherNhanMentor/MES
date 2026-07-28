# Implementation Plan: Get BOM List and BOM Detail (Query BOMs)

**Branch**: `feature/bom-004-query-boms` | **Date**: 2026-07-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/bom-004-query-boms/spec.md` and SRS §3.5

## Summary

Enhance and refine the backend read endpoints for BOM management:
1. `GET /api/boms`: Support paginated listing with optional filtering by `finishedProductId` and `bomStatusId`.
2. `GET /api/boms/{id}`: Return complete BOM detail including header metadata and all component `bom_items`.
3. Update `BomRepository` and `BomPersistenceAdapter` to handle jOOQ conditional filtering (`where(conditions)`).

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21, MapStruct 1.6.3, Lombok, Jakarta Validation  
**Storage**: PostgreSQL 18 (Tables `boms`, `bom_items`, `bom_statuses`, `products`)  
**Testing**: JUnit 5, Mockito, Spring Boot Test (`@ExtendWith(MockitoExtension.class)`, `AbstractIntegrationTest`)  
**Target Platform**: Java Virtual Machine / Docker  
**Project Type**: REST Web Service (Spring MVC WebMVC)  
**Performance Goals**: List queries < 200ms, detail query < 100ms  
**Constraints**: Clean Architecture 4-layer isolation, jOOQ only, MapStruct for DTO mapping  
**Scale/Scope**: Manufacturing Execution System (MES) BOM module  

## Constitution Check

*GATE: All items must pass.*

- [x] **I. Clean Architecture**: Follows exact 4-layer structure (`fpt.qn.mes.bom`).
- [x] **II. jOOQ Only**: Uses jOOQ `DSLContext` with dynamic `Condition` building for filters.
- [x] **III. MapStruct Only for DTOs**: `BomDtoMapper` maps entity to `BomDto` with `items`.
- [x] **IV. Exceptions from Service Layer**: Throws `BomNotFoundException` extending `AppException` (404).
- [x] **V. Consistent API Responses**: `BomController` returns `ResponseEntity<ApiResponse<PageResponse<BomDto>>>` and `ResponseEntity<ApiResponse<BomDto>>`.
- [x] **VI. Mandatory Testing**: Unit tests (`BomServiceTest`) + Integration tests (`BomIntegrationTest`).

## Project Structure

### Documentation (this feature)

```text
specs/bom-004-query-boms/
├── plan.md                 # This file
├── research.md             # Technical research & decisions
├── data-model.md           # DTO & Query parameters
├── quickstart.md           # API testing & validation scenarios
└── contracts/
    └── query-boms-api.json # OpenAPI spec contract
```

### Source Code Layout

```text
be/src/main/java/fpt/qn/mes/bom/
├── domain/
│   └── repository/
│       └── BomRepository.java  # Interface updating findAll method signature
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── BomUseCase.java # Use case interface updating getBoms method
│   ├── service/
│   │   └── BomService.java     # Service implementing filtering & pagination
├── infrastructure/
│   └── persistence/
│       └── BomPersistenceAdapter.java # jOOQ implementation with dynamic Condition
└── presentation/
    └── BomController.java      # GET /api/boms and GET /api/boms/{id}
```

**Structure Decision**: Standard 4-layer Clean Architecture as specified in `docs/rules/backend-architecture.md`.

## Complexity Tracking

*No constitution violations.*
