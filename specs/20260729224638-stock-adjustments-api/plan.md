# Implementation Plan: Stock Adjustments API & Approval Workflow

**Branch**: `005-stock-adjustments-api` | **Date**: 2026-07-29 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/20260729224638-stock-adjustments-api/spec.md`

## Summary

Implement physical inventory adjustment capability (`POST /api/stock-adjustments`) with mandatory non-blank reason validation, non-negative stock balance invariant enforcement (`resultingQuantity >= 0`), immutable `ADJUSTMENT` movement logging in `stock_movements`, and a Factory Manager threshold approval workflow (`stock_adjustment_approvals` table) where approval or rejection deletes the pending request.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21, MapStruct 1.6.3, Flyway
**Storage**: PostgreSQL 18 (tables: `stock_balances`, `stock_movements`, `stock_adjustment_approvals`, `stock_lots`, `users`)
**Testing**: JUnit 5, Mockito, Spring Boot Slice / `@WebMvcTest`, Testcontainers PostgreSQL
**Target Platform**: Linux / Containerized Docker
**Project Type**: Spring Boot Web REST Service (Clean Architecture)
**Performance Goals**: Sub-2 second API roundtrip latency for stock adjustments and approvals
**Constraints**: Zero negative stock balances (`quantity >= 0`), mandatory reason, Clean Architecture layer separation, row-level locking during balance updates
**Scale/Scope**: High-concurrency warehouse execution system

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **I. Clean Architecture**: Follows 4-layer separation: `domain/`, `application/`, `infrastructure/`, `presentation/` in `fpt.qn.mes.inventory`.
- [x] **II. jOOQ Only for Database Access**: Database queries execute via `DSLContext` and generated jOOQ records. No JPA or EntityManager.
- [x] **III. MapStruct Only for Mapping**: Mapping handled via `InventoryDtoMapper` and `StockAdjustmentApprovalRecordMapper`.
- [x] **IV. Exceptions from Service Layer**: Business validation failures throw `AppException` from service layer.
- [x] **V. Consistent API Responses**: Endpoints return `ResponseEntity<ApiResponse<T>>`.
- [x] **VI. Mandatory Tests**: Unit tests for service/domain exceptions and concurrency tests for race condition protection.

## Project Structure

### Documentation (this feature)

```text
specs/20260729224638-stock-adjustments-api/
├── plan.md              # Implementation plan
├── research.md          # Phase 0 architectural decisions & technical patterns
├── data-model.md        # Phase 1 domain entity definitions & DB schema
├── quickstart.md        # Phase 1 validation scenario guide
└── contracts/           # Phase 1 API contracts
    └── stock-adjustments-api.json # OpenAPI 3.0 contract specification
```

### Source Code (repository root)

```text
be/src/main/resources/db/migration/
└── V20260729224500__add_stock_adjustment_approvals.sql

be/src/main/java/fpt/qn/mes/inventory/
├── domain/
│   ├── entities/
│   │   └── StockAdjustmentApproval.java
│   └── repository/
│       └── StockAdjustmentApprovalRepository.java
├── application/
│   ├── dto/
│   │   ├── request/
│   │   │   └── StockAdjustmentRequest.java
│   │   └── response/
│   │       └── StockAdjustmentApprovalDto.java
│   ├── mapper/
│   │   └── StockAdjustmentApprovalDtoMapper.java
│   ├── port/in/
│   │   └── StockAdjustmentUseCase.java
│   └── service/
│       └── StockAdjustmentService.java
├── infrastructure/persistence/
│   ├── StockAdjustmentApprovalRecordMapper.java
│   └── StockAdjustmentApprovalPersistenceAdapter.java
└── presentation/
    └── StockAdjustmentController.java

be/src/test/java/fpt/qn/mes/inventory/
├── domain/
│   └── StockAdjustmentTest.java
├── service/
│   └── StockAdjustmentServiceTest.java
├── presentation/
│   └── StockAdjustmentControllerTest.java
└── integration/
    └── StockAdjustmentIntegrationTest.java
```

**Structure Decision**: Standard 4-layer Clean Architecture module structure inside `fpt.qn.mes.inventory` conforming to constitution rules.

## Complexity Tracking

*No constitution violations. All architecture rules strictly obeyed.*
