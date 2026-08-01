# Implementation Plan: Reserve Work Order Materials

**Branch**: `feature/reserved-materials-wo` | **Date**: 2026-07-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-reserve-work-order-materials/spec.md`

## Summary

Implement the Planner-only `POST /api/v1/work-orders/{id}/reserve-materials` action. The service will resolve the configured `RAW_MATERIAL_WAREHOUSE`, validate the Work Order and requested `machineId`, calculate all remaining material requirements, lock the Work Order/machine/eligible stock rows pessimistically, and either atomically reserve every material and move the Work Order to `READY_TO_PRODUCE` or persist `MATERIAL_SHORTAGE` without changing stock. Every status transition will create a `RESERVE_MATERIAL` audit record.

The design uses a dedicated Work Order reservation use case and persistence output boundary rather than the existing generic inventory movement operation. It also closes the existing traceability and error-contract gaps by mapping `stock_movements.work_order_id`, carrying structured shortage details, and exposing `INSUFFICIENT_STOCK`.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.1.0, Spring Security JWT, jOOQ 3.21, MapStruct 1.6.3, Lombok

**Storage**: PostgreSQL 18 through Flyway migrations

**Testing**: JUnit 5, Mockito, Spring Boot integration tests, Testcontainers PostgreSQL, `ExecutorService` and `CountDownLatch` for concurrency

**Target Platform**: Spring Boot backend service

**Project Type**: Web service

**Performance Goals**: Complete a normal reservation in one database transaction without N+1 repository calls; support the required 20-request concurrency scenario with deterministic results.

**Constraints**: Planner-only access; request contains only `machineId`; source warehouse is resolved by configured code `RAW_MATERIAL_WAREHOUSE`; only `AVAILABLE` stock in that warehouse is eligible; FIFO lot allocation; pessimistic locking; all-or-nothing stock mutation; auditability; standardized `ApiResponse` envelope.

**Scale/Scope**: One Work Order reservation action, its Work Order/material/stock/audit persistence paths, API contract, and required unit/integration/concurrency tests. No new UI is included.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Evaluation | Status |
|---|---|---|
| Clean Architecture | Controller depends on `WorkOrderUseCase`; service owns orchestration; jOOQ remains in infrastructure adapters; cross-module master calls use `*UseCase` interfaces. | PASS |
| jOOQ-only persistence | All new database access will use generated jOOQ tables and `DSLContext`; no JPA, EntityManager, JPQL, or raw SQL. | PASS |
| MapStruct mapping | DTO/domain mapping uses existing or new MapStruct DTO mappers; jOOQ records use dedicated record mappers. | PASS |
| Service-layer exceptions | Validation and shortage exceptions originate in the application service and are handled by `GlobalExceptionHandler`; controller contains no business logic. | PASS |
| API response consistency | Success and error responses use `ResponseEntity<ApiResponse<T>>`; request validation uses `@Valid`. | PASS |
| Mandatory tests | Unit branches, controller authorization, full HTTP/DB integration, shortage, FIFO, warehouse isolation, audit, and required concurrency tests are included in the implementation scope. | PASS |
| Shared mutable state | Work Order, stock balances, reservations, machine availability, and status changes are locked and covered by integration concurrency tests in `WorkOrderIntegrationTest`. | PASS |

No constitution violations require a complexity exception.

## Project Structure

### Documentation (this feature)

```text
specs/004-reserve-work-order-materials/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── reserve-work-order-materials-api.json
└── tasks.md              # Created by /speckit-tasks, not this plan
```

### Source Code (repository root)

```text
be/src/main/java/fpt/qn/mes/
├── workorder/
│   ├── application/
│   │   ├── dto/reservation/reserve/
│   │   │   ├── ReserveWorkOrderMaterialsRequest.java
│   │   │   └── ReserveWorkOrderMaterialsResponse.java
│   │   ├── exception/
│   │   │   ├── InsufficientMaterialException.java
│   │   │   ├── InvalidWorkOrderReservationException.java
│   │   │   └── MachineNotAvailableException.java
│   │   ├── port/in/WorkOrderUseCase.java
│   │   ├── port/out/WorkOrderReservationPort.java
│   │   ├── port/out/AuditLogPort.java
│   │   └── service/WorkOrderService.java
│   ├── domain/
│   │   ├── constants/WorkOrderStatusConstants.java
│   │   └── repository/WorkOrderRepository.java
│   ├── infrastructure/persistence/
│   │   ├── WorkOrderReservationPersistenceAdapter.java
│   │   ├── AuditLogPersistenceAdapter.java
│   │   └── StockMovementRecordMapper.java
│   └── presentation/WorkOrderController.java
├── master/warehouse/
│   ├── application/port/in/WarehouseUseCase.java
│   └── infrastructure/persistence/WarehousePersistenceAdapter.java
├── master/machine/
│   ├── application/port/in/MachineUseCase.java
│   └── infrastructure/persistence/MachinePersistenceAdapter.java
└── common/exception/
    ├── AppException.java
    ├── ErrorCode.java
    └── GlobalExceptionHandler.java

be/src/test/java/fpt/qn/mes/
├── workorder/presentation/WorkOrderControllerTest.java
├── workorder/application/service/WorkOrderServiceTest.java
└── workorder/integration/WorkOrderIntegrationTest.java
```

**Structure Decision**: Extend the existing `workorder` module with an operation-specific request/response DTO package, input-port method, service orchestration, and output ports. Implement cross-table reservation and audit persistence in Work Order infrastructure adapters so the application layer does not import Inventory repositories or persistence classes, preserving the existing dependency DAG. Extend master use-case interfaces only for warehouse-code and machine-availability semantics needed by Work Order.

## Phase 0: Research Findings

Research is complete in [research.md](research.md). Key resolved decisions:

- Dedicated Work Order reservation orchestration instead of generic Inventory movement service.
- Server-side warehouse resolution by `RAW_MATERIAL_WAREHOUSE`.
- Pessimistic locks on Work Order, machine, and eligible stock balances in deterministic order.
- All-or-nothing reservation with a non-rollback shortage path that persists `MATERIAL_SHORTAGE` and audit data.
- Structured `INSUFFICIENT_STOCK` details and `stock_movements.work_order_id` traceability.
- Versioned route exposed without removing current `/api/work-orders` routes.

## Phase 1: Design Summary

### Data and State

The existing `work_orders`, `work_order_materials`, `stock_balances`, `stock_lots`, `stock_movements`, `machines`, `warehouses`, and `audit_logs` tables are reused. No new business table is required. Existing schema support is completed in application/domain mapping where `stock_movements.work_order_id` is currently not represented.

The valid transitions are `PLANNED → READY_TO_PRODUCE` and `MATERIAL_SHORTAGE → READY_TO_PRODUCE` on success. A shortage persists `MATERIAL_SHORTAGE` and leaves all stock/material quantities unchanged.

### API Contract

The public contract is defined in [contracts/reserve-work-order-materials-api.json](contracts/reserve-work-order-materials-api.json). The request contains only `machineId`; the source warehouse is never client-controlled. Successful responses contain the Work Order ID and `READY_TO_PRODUCE` status. Shortage responses contain structured per-material details.

### Persistence and Concurrency

The reservation adapter will issue one bounded/joined jOOQ query to load and lock eligible FIFO candidates for all material product IDs, rather than querying inside a material loop. It will lock in deterministic order, validate aggregate quantities, update source/destination status balances, and insert movement rows linked to the Work Order. The service owns the transaction boundary.

### Error and Audit Handling

Add the `INSUFFICIENT_STOCK` error code and structured details support to the common error path. Add module-scoped exceptions for invalid reservation state, unavailable machine, missing configuration, and insufficient material. Audit writes use the existing `audit_logs` table and are part of the same success/shortage transaction.

## Phase 1 Constitution Re-check

| Gate | Result | Evidence |
|---|---|---|
| Layer boundaries | PASS | Output ports isolate cross-table persistence; controller delegates to input port. |
| Persistence technology | PASS | jOOQ `DSLContext` and generated tables only. |
| Mapping | PASS | DTO and record mapping remain separated. |
| Transactions | PASS | Service owns one transaction; expected shortage exception is explicitly handled so status/audit persist without stock mutation. |
| API/security | PASS | `@Valid`, Planner authorization, UUID path, and `ApiResponse` envelope. |
| Testing | PASS | Unit, HTTP/DB integration, authorization, FIFO, shortage, audit, warehouse scope, and concurrency coverage planned. |

## Complexity Tracking

No constitution violations. No complexity exceptions are required.
