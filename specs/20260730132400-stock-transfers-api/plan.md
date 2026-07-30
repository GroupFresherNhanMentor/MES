# Implementation Plan: Stock Transfers API

**Branch**: `20260730132400-stock-transfers-api` | **Date**: 2026-07-30 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/20260730132400-stock-transfers-api/spec.md`

## Summary

Implement `POST /api/stock-transfers` in the `inventory` module to transfer an AVAILABLE quantity of a product lot from a source warehouse location to a destination warehouse location. The service will validate stock availability, atomically deduct source stock balance and increase destination stock balance, and create two corresponding movement logs (`TRANSFER_OUT` and `TRANSFER_IN`).

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.1.0, MapStruct 1.6.3, Lombok, jOOQ 3.21.5
**Storage**: PostgreSQL 18
**Testing**: JUnit 5, Mockito, Spring Boot `@WebMvcTest`, Testcontainers PostgreSQL
**Target Platform**: Linux / Spring Boot REST API
**Project Type**: Backend REST Web Service (Clean Architecture)
**Performance Goals**: Sub-50ms transaction processing for stock transfer
**Constraints**: Atomic transaction execution, strict Clean Architecture layer flow, concurrency test verification

## Constitution Check

*GATE: Passed. Re-checked after Phase 1 design.*

- **Clean Architecture Layering**: All transfer request/response DTOs reside in `application/dto/`, domain interface in `application/port/in/InventoryUseCase`, service logic in `application/service/InventoryService`, jOOQ persistence adapters in `infrastructure/persistence/`, REST controller in `presentation/InventoryController`.
- **jOOQ Only**: All database operations for stock balance updates and movement insertion use jOOQ `DSLContext`.
- **MapStruct Only**: MapStruct mappers used for DTO-entity-record mapping.
- **Service Exceptions**: Throws `InsufficientStockException` / `AppException` from service layer.
- **Consistent API Response**: Controller returns `ResponseEntity<ApiResponse<StockTransferResponse>>`.
- **Mandatory Tests & Concurrency**: Includes unit tests, controller slice tests, and concurrency integration tests with `CountDownLatch` + `ExecutorService`.

## Project Structure

### Documentation (this feature)

```text
specs/20260730132400-stock-transfers-api/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan
├── research.md          # Phase 0 research findings
├── data-model.md        # Phase 1 data model
├── quickstart.md        # Phase 1 validation guide
├── contracts/           # API contracts
│   └── stock-transfer-api.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

```text
be/src/main/java/fpt/qn/mes/inventory/
├── application/
│   ├── dto/
│   │   ├── request/
│   │   │   └── StockTransferRequest.java
│   │   └── response/
│   │       └── StockTransferResponse.java
│   ├── port/in/
│   │   └── InventoryUseCase.java
│   └── service/
│       └── InventoryService.java
└── presentation/
    └── InventoryController.java

be/src/test/java/fpt/qn/mes/inventory/
├── service/
│   └── InventoryServiceTest.java
├── presentation/
│   └── InventoryControllerTest.java
└── StockTransferIntegrationTest.java
```

**Structure Decision**: Clean Architecture single-module structure inside `be/src/main/java/fpt/qn/mes/inventory/`.

## Complexity Tracking

*No constitution violations.*
