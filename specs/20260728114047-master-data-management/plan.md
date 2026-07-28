# Implementation Plan: Master Data Management

**Branch**: `20260728114047-master-data-management` | **Date**: 2026-07-28 | **Spec**: `specs/20260728114047-master-data-management/spec.md`

**Input**: Feature specification from `specs/20260728114047-master-data-management/spec.md`

## Summary

Implement Master Data Management for FactoryFlow: CRUD + business rules for 5 core master data entities (Product, Warehouse, WarehouseLocation, ProductionLine, Machine) with status lifecycle management, duplicate-code enforcement, soft-delete guards, machine status state machine, and paginated listing — all behind ADMIN role authorization. The module structure already exists under `be/src/main/java/fpt/qn/mes/master/` with 4-layer Clean Architecture stubs; the plan fills in all implementation.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21, MapStruct 1.6.3, Lombok, Flyway, Spring Security OAuth2 Resource Server (JWT)

**Storage**: PostgreSQL 18 (via Flyway migrations + jOOQ DSL)

**Testing**: JUnit 5 + Mockito (unit), Testcontainers via `AbstractIntegrationTest` (integration), `CountDownLatch` + `ExecutorService` (concurrency)

**Target Platform**: Linux server (Docker Compose for PostgreSQL, Spring Boot JAR)

**Project Type**: Web service — Spring Boot REST API with monorepo `be/` backend

**Performance Goals**: <1s for duplicate-code validation, paginated list endpoints with default page size 20, optimistic locking for concurrent product updates

**Constraints**: All entities UUID (app-generated), mandatory audit columns (created_at/created_by/updated_at/updated_by), soft-delete only (no hard delete), ADMIN role required for CUD operations, role ADMIN required for CUD operations, read access may extend to other roles

**Scale/Scope**: 5 master entities (Product, Warehouse, WarehouseLocation, ProductionLine, Machine) + 5 lookup tables (product_types, product_statuses, units_of_measure, warehouse_statuses NEEDS CLARIFICATION, machine_statuses)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Clean Architecture is Non-Negotiable | ✅ PASS | 4-layer structure already exists per sub-module (domain/application/infrastructure/presentation) |
| II. jOOQ Only for Database Access | ✅ PASS | Persistence adapters use DSLContext via BaseRepository |
| III. MapStruct Only for Mapping | ✅ PASS | DtoMapper interfaces exist; RecordMapper classes exist |
| IV. Exceptions from Service Layer | ✅ PASS | *NotFoundException classes exist per sub-module |
| V. Consistent API Responses | ✅ PASS | ApiResponse<T> pattern used in controllers |
| VI. Tests Are Mandatory | ✅ PASS | Will produce test tasks for every user story |
| VII. Naming Conventions | ✅ PASS | Classes follow naming.md patterns |
| VIII. Flyway + Seed Data | ✅ PASS | DB schema exists; seed JSON needed for lookup tables |

**Gates**: All pass. No violations.
- ERROR if Clean Architecture is violated — no violation detected (existing structure is correct)
- ERROR if tests are omitted — will be enforced in task generation
- No complexity tracking needed (standard CRUD, no unusual patterns)

## Project Structure

### Documentation (this feature)

```text
specs/20260728114047-master-data-management/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
be/src/main/java/fpt/qn/mes/master/
├── product/                 # Product entity
│   ├── domain/entities/Product.java
│   ├── domain/repository/ProductRepository.java
│   ├── application/port/in/ProductUseCase.java
│   ├── application/service/ProductService.java
│   ├── application/dto/request/CreateProductRequest.java
│   ├── application/dto/request/UpdateProductRequest.java
│   ├── application/dto/response/ProductDto.java
│   ├── application/mapper/ProductDtoMapper.java
│   ├── application/exception/ProductNotFoundException.java
│   ├── infrastructure/persistence/ProductPersistenceAdapter.java
│   ├── infrastructure/persistence/ProductRecordMapper.java
│   └── presentation/ProductController.java
├── warehouse/               # Warehouse entity (same structure)
│   └── ...
├── location/                # WarehouseLocation entity (same structure)
│   └── ...
├── production-line/         # ProductionLine entity (same structure)
│   └── ...
└── machine/                 # Machine entity (same structure)
    └── ...

be/src/main/resources/
├── db/migration/V20260726194837__init.sql   # Schema (exists)
├── db/migration/V2__seed_master_data.sql     # NEW: seed lookup data
└── seed/
    ├── product-types.json                    # NEW
    ├── product-statuses.json                 # NEW
    ├── units-of-measure.json                 # NEW
    ├── warehouse-statuses.json               # NEW
    └── machine-statuses.json                 # NEW
```

**Structure Decision**: Follow existing `master/` sub-module layout — each of the 5 entities gets its own Clean Architecture package. The directory structure is already scaffolded; this plan fills in the implementation.

## Complexity Tracking

> No violations — standard CRUD + status state machine, all within well-established Clean Architecture patterns.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| — | — | — |
