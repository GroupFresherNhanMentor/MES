# Implementation Plan: Create Bill of Materials Header (Create BOM Header)

**Branch**: `bom-001-create-header` | **Date**: 2026-07-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/bom-001-create-header/spec.md`

## Summary

Implement the backend capability for Planners to create a new Bill of Materials (BOM) header in `DRAFT` status for finished goods or semi-finished goods. The implementation follows the strict 4-layer Clean Architecture (`domain`, `application`, `infrastructure`, `presentation`), using Spring Boot 4.1.0, Java 25, jOOQ 3.21, and PostgreSQL 18.

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21, MapStruct 1.6.3, Lombok, Jakarta Validation  
**Storage**: PostgreSQL 18 (Table `boms`, `bom_statuses`, `products`)  
**Testing**: JUnit 5, Mockito, Spring Boot Test (`@ExtendWith(MockitoExtension.class)`, `AbstractIntegrationTest`)  
**Target Platform**: Java Virtual Machine / Docker  
**Project Type**: REST Web Service (Spring MVC WebMVC)  
**Performance Goals**: Response time < 500ms for creation endpoint  
**Constraints**: Clean Architecture 4-layer isolation, jOOQ only (no JPA/Hibernate), MapStruct for DTO mapping, no custom SQL strings  
**Scale/Scope**: Manufacturing Execution System (MES) BOM module  

## Constitution Check

*GATE: All items must pass.*

- [x] **I. Clean Architecture**: Follows exact 4-layer structure (`fpt.qn.mes.bom`).
- [x] **II. jOOQ Only**: Uses jOOQ `DSLContext` and generated `BomsRecord`.
- [x] **III. MapStruct Only for DTOs**: `BomDtoMapper` uses MapStruct; `BomRecordMapper` is a manual `@Component`.
- [x] **IV. Exceptions from Service Layer**: Throws `BomAlreadyExistsException` / `InvalidBomProductTypeException` extending `AppException`.
- [x] **V. Consistent API Responses**: `BomController` returns `ResponseEntity<ApiResponse<BomDto>>`.
- [x] **VI. Mandatory Testing**: Unit tests (`BomServiceTest`) + Integration tests (`BomIntegrationTest`).

## Project Structure

### Documentation (this feature)

```text
specs/bom-001-create-header/
├── plan.md              # This file
├── research.md          # Technical research & decisions
├── data-model.md        # Entity definitions & schema mappings
├── quickstart.md        # API testing scenarios
└── contracts/
    └── create-bom-api.json # OpenAPI spec contract
```

### Source Code Layout

```text
be/src/main/java/fpt/qn/mes/bom/
├── domain/
│   ├── entities/
│   │   └── Bom.java
│   └── repository/
│       └── BomRepository.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── BomUseCase.java
│   ├── service/
│   │   └── BomService.java
│   ├── dto/
│   │   ├── request/
│   │   │   └── CreateBomRequest.java
│   │   └── response/
│   │       └── BomDto.java
│   ├── mapper/
│   │   └── BomDtoMapper.java
│   └── exception/
│       ├── BomAlreadyExistsException.java
│       └── InvalidBomProductTypeException.java
├── infrastructure/
│   └── persistence/
│       ├── BomPersistenceAdapter.java
│       └── BomRecordMapper.java
└── presentation/
    └── BomController.java

be/src/test/java/fpt/qn/mes/bom/
├── service/
│   └── BomServiceTest.java
└── integration/
    └── BomIntegrationTest.java
```

**Structure Decision**: Standard 4-layer Clean Architecture as specified in `docs/rules/backend-architecture.md`.

## Complexity Tracking

*No constitution violations.*
