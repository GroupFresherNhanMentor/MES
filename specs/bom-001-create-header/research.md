# Research: Create Bill of Materials Header (Create BOM Header)

**Feature**: `bom-001-create-header`  
**Date**: 2026-07-28  
**Spec**: [spec.md](spec.md)

## Research Findings & Architectural Decisions

### Decision 1: Architecture & Layer Dependency Strategy
- **Decision**: Strictly follow Clean Architecture 4-layer structure (`domain/`, `application/`, `infrastructure/`, `presentation/`).
- **Rationale**: Mandated by project `constitution.md` and `docs/rules/backend-architecture.md`.
- **Alternatives Considered**: 
  - Direct database access from controllers or services (Rejected: violates clean architecture).

### Decision 2: Product Type Validation Strategy
- **Decision**: Validate that `finishedProductId` exists and belongs to product type `FINISHED_GOOD` or `SEMI_FINISHED` before creating a BOM header.
- **Rationale**: Business rule FR-005 dictates that Raw Materials, Consumables, and Spare Parts cannot have BOMs.
- **Cross-Module Access**: Depend on `master/product/application/port/in/ProductUseCase.java` interface to query product details.
- **Alternatives Considered**: 
  - Importing `ProductRepository` or `Product` entity directly in `bom` module (Rejected: violates cross-module dependency rules in `backend-architecture.md`).

### Decision 3: Identity & Audit Log Strategy
- **Decision**: Obtain `currentUserId` in `BomService` via `CurrentUserPort` (from `auth/application/port/out/CurrentUserPort.java`).
- **Rationale**: `constitution.md` and `backend-patterns.md` state controllers should NOT pass `userId` as an argument; services fetch it from `CurrentUserPort`.
- **Alternatives Considered**: 
  - Passing `userId` from `@AuthenticationPrincipal` in REST controller (Rejected: forbidden by backend rules).

### Decision 4: Initial Status & Version Constraint Strategy
- **Decision**: New BOM headers are created with `bomStatusId` corresponding to `DRAFT`. Unique constraint `(finished_product_id, version)` is enforced at both DB level (`boms` table `uq_boms_product_version`) and service level (`BomAlreadyExistsException`).
- **Rationale**: Prevents duplicate versions for the same product and ensures consistency.

### Decision 5: Persistence Technology Choice
- **Decision**: Use jOOQ `DSLContext` for all SQL operations inside `BomPersistenceAdapter` extending `BaseRepository<BomsRecord>`.
- **Rationale**: Spring Data JPA / Hibernate are strictly forbidden by `constitution.md`.
