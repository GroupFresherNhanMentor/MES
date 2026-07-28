# Implementation Plan: Bill of Materials Versioning (BOM Versioning)

**Branch**: `feature/bom-003-bom-versioning` | **Date**: 2026-07-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/bom-003-bom-versioning/spec.md` and SRS §3.5 FR-BOM-003

## Summary

Implement the backend capability for BOM Versioning (`POST /api/boms/{id}/new-version`):
1. Cloning an existing source BOM (ACTIVE or INACTIVE) into a new `DRAFT` BOM version.
2. Auto-calculating `version = maxVersion + 1` for the target product.
3. Deep-copying all `bom_items` (`materialProductId`, `quantityPerUnit`, `scrapRate`) from source to new BOM.
4. Enforcing immutability rules: Blocking direct item additions or deletions (`POST /api/boms/{bomId}/items` and `DELETE /api/boms/{bomId}/items/{itemId}`) when BOM status is not `DRAFT` by throwing `InvalidBomStatusException`.

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21, MapStruct 1.6.3, Lombok, Jakarta Validation  
**Storage**: PostgreSQL 18 (Tables `boms`, `bom_items`, `bom_statuses`, `products`)  
**Testing**: JUnit 5, Mockito, Spring Boot Test (`@ExtendWith(MockitoExtension.class)`, `AbstractIntegrationTest`)  
**Target Platform**: Java Virtual Machine / Docker  
**Project Type**: REST Web Service (Spring MVC WebMVC)  
**Performance Goals**: Response time < 500ms for cloning complex BOMs with 50+ items  
**Constraints**: Clean Architecture 4-layer isolation, jOOQ only (no JPA/Hibernate), MapStruct for DTO mapping, no custom SQL strings  
**Scale/Scope**: Manufacturing Execution System (MES) BOM module  

## Constitution Check

*GATE: All items must pass.*

- [x] **I. Clean Architecture**: Follows exact 4-layer structure (`fpt.qn.mes.bom`).
- [x] **II. jOOQ Only**: Uses jOOQ `DSLContext` for max version lookup & bulk item cloning.
- [x] **III. MapStruct Only for DTOs**: `BomDtoMapper` uses MapStruct for entity ↔ DTO mapping.
- [x] **IV. Exceptions from Service Layer**: Throws `BomNotFoundException` / `InvalidBomStatusException` extending `AppException`.
- [x] **V. Consistent API Responses**: `BomController` returns `ResponseEntity<ApiResponse<BomDto>>`.
- [x] **VI. Mandatory Testing**: Unit tests (`BomServiceTest`) + Integration tests (`BomIntegrationTest`).

## Project Structure

### Documentation (this feature)

```text
specs/bom-003-bom-versioning/
├── plan.md                 # This file
├── research.md             # Technical research & decisions
├── data-model.md           # Entity definitions & schema mappings
├── quickstart.md           # API testing & validation scenarios
└── contracts/
    └── bom-versioning-api.json # OpenAPI spec contract
```

### Source Code Layout

```text
be/src/main/java/fpt/qn/mes/bom/
├── domain/
│   ├── entities/
│   │   └── Bom.java             # Entity with clone/createVersion factory
│   └── repository/
│       └── BomRepository.java  # Repository with findMaxVersion & copyItems methods
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── BomUseCase.java # Use case interface adding createNewVersion
│   ├── service/
│   │   └── BomService.java     # Service implementing transactional cloning & immutability guards
│   ├── dto/
│   │   └── response/
│   │       └── BomDto.java
│   └── exception/
│       └── InvalidBomStatusException.java # Exception thrown on editing non-DRAFT BOMs
├── infrastructure/
│   └── persistence/
│       └── BomPersistenceAdapter.java # jOOQ implementation of max version & item cloning
└── presentation/
    └── BomController.java      # POST /api/boms/{id}/new-version endpoint
```

**Structure Decision**: Standard 4-layer Clean Architecture as specified in `docs/rules/backend-architecture.md`.

## Complexity Tracking

*No constitution violations.*
