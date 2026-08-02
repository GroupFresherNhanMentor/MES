# Implementation Plan: Create Work Order (`POST /api/work-orders`)

**Branch**: `wo-002-create-work-order` | **Date**: 2026-07-29 | **Spec**: [spec.md](file:///D:/Programming/FPT-Software/OJT/MES/MES/specs/wo-002-create-work-order/spec.md)

**Input**: Feature specification from `/specs/wo-002-create-work-order/spec.md`

## Summary

Implement the `POST /api/work-orders` endpoint to create a new Work Order. The implementation verifies that the user possesses `ROLE_PLANNER`, validates that `plannedQuantity > 0` and `plannedEndDate > plannedStartDate`, verifies that the finished product has an `ACTIVE` BOM (returning error `BOM_NOT_ACTIVE` if missing), automatically links the active `bomId` to the Work Order, calculates material requirements for each BOM component using `requiredQuantity = plannedQuantity * quantityPerUnit * (1 + scrapRate)` and saves them to `work_order_materials`, sets initial status to `PLANNED`, and records an audit log with action `CREATE_WORK_ORDER`.

## Technical Context

**Language/Version**: Java 25 (Spring Boot 4.1.0)
**Primary Dependencies**: Spring Boot Starter WebMVC, Spring Security OAuth2 Resource Server, jOOQ 3.21, MapStruct 1.6.3, Lombok
**Storage**: PostgreSQL 18 with jOOQ DSLContext persistence adapter
**Testing**: JUnit 5, Mockito
**Target Platform**: Linux / Windows Spring Boot Service
**Project Type**: REST Web Service (Clean Architecture)
**Performance Goals**: Work Order creation + material requirements calculation < 300ms
**Constraints**: Clean Architecture rules (domain Entities pure Java without framework annotations, Application layer use-cases and DTOs, Infrastructure jOOQ persistence adapter, Presentation REST Controller returning `ApiResponse<T>`), NO Method References (use Lambdas).
**Scale/Scope**: Manufacturing execution system for work orders and material allocations.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Layer dependencies flow inwards: `presentation` -> `application.port.in` -> `domain` <- `application.service` <- `infrastructure.persistence`. **PASSED**
- No framework annotations in domain entities (`WorkOrder`, `WorkOrderMaterial`, `WorkOrderEvent`). **PASSED**
- No Java Records, No Lombok on domain entities, No method references (`Bom::getId` forbidden, use `b -> b.getId()`). **PASSED**
- All controller responses wrapped in `ApiResponse<T>`. **PASSED**
- Role-based authorization enforced via `@PreAuthorize("hasRole('PLANNER')")`. **PASSED**

## Project Structure

### Documentation (this feature)

```text
specs/wo-002-create-work-order/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan
├── research.md          # Technical research & decisions
├── data-model.md        # Domain entities, records, and DTO definitions
├── quickstart.md        # End-to-end verification guide
└── contracts/           # API contract schemas
    └── post-work-order.json
```

### Source Code (repository root)

```text
be/src/main/java/fpt/qn/mes/
├── workorder/
│   ├── domain/
│   │   ├── entities/
│   │   │   ├── WorkOrder.java
│   │   │   └── WorkOrderMaterial.java
│   │   └── repository/
│   │       └── WorkOrderRepository.java
│   ├── application/
│   │   ├── port/in/
│   │   │   └── WorkOrderUseCase.java
│   │   ├── service/
│   │   │   └── WorkOrderService.java
│   │   ├── dto/
│   │   │   ├── request/CreateWorkOrderRequest.java
│   │   │   └── response/WorkOrderDto.java
│   │   ├── mapper/
│   │   │   └── WorkOrderDtoMapper.java
│   │   └── exception/
│   │       ├── BomNotActiveException.java
│   │       └── WorkOrderCodeExistsException.java
│   ├── infrastructure/
│   │   └── persistence/
│   │       ├── WorkOrderPersistenceAdapter.java
│   │       └── WorkOrderRecordMapper.java
│   └── presentation/
│       └── WorkOrderController.java
└── bom/
    └── domain/
        └── repository/
            └── BomRepository.java
```

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Cross-module lookup (`BomRepository` in `WorkOrderService`) | Need to find active BOM and BOM items to calculate material requirements | Duplicating BOM data in WorkOrder table would violate normalization and data integrity |
