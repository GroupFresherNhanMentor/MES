# Implementation Plan: Query Work Orders (`GET /api/work-orders`)

**Branch**: `wo-001-query-work-orders` | **Date**: 2026-07-28 | **Spec**: [spec.md](file:///D:/Programming/FPT-Software/OJT/MES/MES/specs/wo-001-query-work-orders/spec.md)

**Input**: Feature specification from `specs/wo-001-query-work-orders/spec.md`

## Summary

Implement the `GET /api/work-orders` REST API endpoint in the Spring Boot backend using Clean Architecture (Presentation ➔ Application Ports ➔ Service ➔ Domain ➔ jOOQ Persistence Adapter). The endpoint supports 0-based pagination, dynamic filtering by `finishedProductId` and `statusId`, case-insensitive fuzzy trigram search on `code`, and role-based access control (RBAC) via Spring Security for roles `ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, and `AUDITOR`.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.1.0, Spring Security + OAuth2 Resource Server, jOOQ 3.21, MapStruct 1.6.3, Lombok

**Storage**: PostgreSQL 18 with `pg_trgm` extension

**Testing**: JUnit 5, Mockito, Testcontainers

**Target Platform**: Linux / Windows JVM (Java 25)

**Project Type**: Spring Boot Web REST Service (Backend Only)

**Performance Goals**: Sub-500ms p95 response time for paginated queries up to 100k work order records

**Constraints**: Clean Architecture layers, No Lombok on domain entities, No Java Records, No Method References, No JPA/Hibernate, explicit constructor injection, standard `ApiResponse<T>` envelope

**Scale/Scope**: 1 REST Endpoint (`GET /api/work-orders`) with 5 query parameters (`page`, `size`, `finishedProductId`, `statusId`, `code`)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Clean Architecture layer separation enforced (Domain → Application → Persistence Adapter → REST Presentation)
- [x] No ORM magic (jOOQ DSL used exclusively)
- [x] Spring Security RBAC configured and `@PreAuthorize` used
- [x] Unified `ApiResponse<T>` envelope used
- [x] Strict Java 25 style rules (no method references, lambdas only, no Lombok on domain entities)

## Project Structure

### Documentation (this feature)

```text
specs/wo-001-query-work-orders/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan (/speckit-plan output)
├── research.md          # Phase 0 output (/speckit-plan output)
├── data-model.md        # Phase 1 output (/speckit-plan output)
├── quickstart.md        # Phase 1 output (/speckit-plan output)
└── contracts/           # Phase 1 output (/speckit-plan output)
    └── get-work-orders.json
```

### Source Code (repository root)

```text
be/src/main/java/fpt/qn/mes/
├── workorder/
│   ├── domain/
│   │   ├── entities/
│   │   │   └── WorkOrder.java
│   │   └── repository/
│   │       └── WorkOrderRepository.java
│   ├── application/
│   │   ├── dto/
│   │   │   └── response/WorkOrderDto.java
│   │   ├── mapper/
│   │   │   └── WorkOrderDtoMapper.java
│   │   ├── port/in/
│   │   │   └── WorkOrderUseCase.java
│   │   └── service/
│   │       └── WorkOrderService.java
│   ├── infrastructure/
│   │   └── persistence/
│   │       ├── WorkOrderRecordMapper.java
│   │       └── WorkOrderPersistenceAdapter.java
│   └── presentation/
│       └── WorkOrderController.java
```

**Structure Decision**: Clean Architecture 4-layer structure inside `fpt.qn.mes.workorder`.

## Complexity Tracking

> No constitution violations.
