# Implementation Plan: Add and Remove BOM Items

**Branch**: `feature/bom-005-add-remove-bom-items` | **Date**: 2026-07-28 | **Spec**: [spec.md](file:///e:/Fresher/ojt/MES/specs/bom-005-add-remove-bom-items/spec.md)

**Input**: Feature specification from `/specs/bom-005-add-remove-bom-items/spec.md`

## Summary

This feature provides REST endpoints to add (`POST /api/boms/{bomId}/items`) and remove (`DELETE /api/boms/{bomId}/items/{itemId}`) component line items on a Bill of Materials. Modification is strictly restricted to BOM headers in `DRAFT` status; attempts to modify `ACTIVE` or `INACTIVE` BOMs are rejected with HTTP 400 (`InvalidBomStatusException`). Access is secured for roles `ADMIN` and `PLANNER`.

## Technical Context

**Language/Version**: Java 25 / Spring Boot 4.1.0  
**Primary Dependencies**: Spring Web, Spring Security, MapStruct 1.6.3, jOOQ 3.21.5, Lombok  
**Storage**: PostgreSQL 18 (jOOQ persistence via `bom_items` table)  
**Testing**: JUnit 5, Mockito  
**Target Platform**: Java Virtual Machine (JVM) backend service  
**Project Type**: Spring Boot Web REST Service (Clean Architecture)  
**Performance Goals**: Item additions and removals complete in < 100ms  
**Constraints**: Immutability guard for non-DRAFT BOMs; role-based access control (`ADMIN`, `PLANNER`)  
**Scale/Scope**: Single component modification per API call  

## Constitution Check

- Clean Architecture 4-layer isolation: `presentation` -> `application.port.in` -> `domain` <- `application.service` -> `domain.repository` <- `infrastructure.persistence`
- No JPA/Hibernate: jOOQ only
- Security: Role check `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")`

## Project Structure

### Documentation (this feature)

```text
specs/bom-005-add-remove-bom-items/
├── plan.md              # This file
├── research.md          # Phase 0 research findings
├── data-model.md        # Data model and validation specs
├── quickstart.md        # Validation scenarios guide
└── contracts/
    └── add-remove-bom-items-api.json  # OpenAPI contract specification
```

### Source Code

```text
be/src/main/java/fpt/qn/mes/bom/
├── application/
│   ├── dto/
│   │   ├── request/
│   │   │   └── CreateBomItemRequest.java
│   │   └── response/
│   │       └── BomItemDto.java
│   ├── exception/
│   │   ├── BomNotFoundException.java
│   │   └── InvalidBomStatusException.java
│   ├── mapper/
│   │   └── BomDtoMapper.java
│   ├── port/
│   │   └── in/
│   │       └── BomUseCase.java
│   └── service/
│       └── BomService.java
├── domain/
│   ├── entities/
│   │   ├── Bom.java
│   │   └── BomItem.java
│   └── repository/
│       └── BomRepository.java
├── infrastructure/
│   └── persistence/
│       └── BomPersistenceAdapter.java
└── presentation/
    └── BomController.java
```

**Structure Decision**: Standard Clean Architecture module layout in Spring Boot backend.
