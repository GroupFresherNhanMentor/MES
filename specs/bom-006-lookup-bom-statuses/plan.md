# Implementation Plan: Get BOM Statuses (Lookup)

**Branch**: `feature/bom-006-lookup-bom-statuses` | **Date**: 2026-07-28 | **Spec**: [spec.md](file:///e:/Fresher/ojt/MES/specs/bom-006-lookup-bom-statuses/spec.md)

**Input**: Feature specification from `/specs/bom-006-lookup-bom-statuses/spec.md`

## Summary

This feature implements the `GET /api/boms/statuses` endpoint to return the master lookup entries (`id`, `name`, `description`) from the `bom_statuses` database table. It leverages the existing `LookupRepository.findAll("bom_statuses")` infrastructure in `fpt.qn.mes.common.service` and is available to all authenticated users.

## Technical Context

**Language/Version**: Java 25 / Spring Boot 4.1.0  
**Primary Dependencies**: Spring Web, Spring Security, jOOQ 3.21.5, Lombok  
**Storage**: PostgreSQL 18 (read query from `bom_statuses` table)  
**Testing**: JUnit 5, Mockito  
**Target Platform**: Java Virtual Machine (JVM) backend service  
**Project Type**: Spring Boot Web REST Service (Clean Architecture)  
**Performance Goals**: Lookup query completes in < 50ms  
**Constraints**: Open to all authenticated roles (`@PreAuthorize("isAuthenticated()")`)  
**Scale/Scope**: Small, fixed set of status rows (no pagination)  

## Constitution Check

- Clean Architecture isolation: Uses shared `LookupRepository` in `common.service`
- No JPA/Hibernate: jOOQ `select` query via `LookupRepository`
- Security: Role check `@PreAuthorize("isAuthenticated()")`

## Project Structure

### Documentation (this feature)

```text
specs/bom-006-lookup-bom-statuses/
├── plan.md              # This file
├── research.md          # Phase 0 research findings
├── data-model.md        # Data model and DTO specs
├── quickstart.md        # Validation scenarios guide
└── contracts/
    └── lookup-bom-statuses-api.json  # OpenAPI contract specification
```

### Source Code

```text
be/src/main/java/fpt/qn/mes/
├── common/
│   └── service/
│       ├── LookupEntry.java
│       └── LookupRepository.java
└── bom/
    ├── application/
    │   ├── port/
    │   │   └── in/
    │   │       └── BomUseCase.java
    │   └── service/
    │       └── BomService.java
    └── presentation/
        └── BomController.java
```

**Structure Decision**: Standard Clean Architecture module layout referencing `common.service.LookupRepository`.
