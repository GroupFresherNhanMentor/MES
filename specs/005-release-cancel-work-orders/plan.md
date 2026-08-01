# Implementation Plan: Release and Cancel Work Orders

**Branch**: `005-release-cancel-work-orders` | **Date**: 2026-07-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/005-release-cancel-work-orders/spec.md`

## Summary

Implement backend endpoints `POST /api/v1/work-orders/{id}/release-materials` and `POST /api/v1/work-orders/{id}/cancel` in the Spring Boot 4-layer Clean Architecture.
The implementation will handle stock balance updates (`RESERVED` → `AVAILABLE`), `RELEASE_RESERVATION` stock movement logging, work order status transitions to `CANCELLED`, multi-lot traceability by querying historical `RESERVE` movements, and pessimistic database locking (`SELECT FOR UPDATE`) to prevent race conditions.

## Technical Context

**Language/Version**: Java 25, Spring Boot 4.1.0
**Primary Dependencies**: Spring Boot Web Starter, Spring Security OAuth2, jOOQ 3.21, MapStruct 1.6.3, Lombok
**Storage**: PostgreSQL 18
**Testing**: JUnit 5, Mockito, Spring Boot Integration Tests
**Target Platform**: JVM / Linux Server
**Project Type**: REST Web Service
**Performance Goals**: Sub-second execution (<500ms p95), thread-safe under 100+ concurrent requests
**Constraints**: Clean Architecture 4-layer rules (`docs/rules/backend-architecture.md`), jOOQ query patterns (`docs/rules/backend-query-optimization.md`), no Hibernate/JPA ORM.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Clean Architecture Layering**: All logic isolated in `application.service`, ports in `application.port.in` and `application.port.out`, persistence adapters in `infrastructure.persistence.adapter`.
- [x] **Database Schema**: Existing tables `work_orders`, `stock_balances`, `stock_movements`, `work_order_materials` utilized without modifying Flyway migrations (seed data already contains `RELEASE_RESERVATION` movement type).
- [x] **Security Constraints**: Endpoints secured with `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")`.
- [x] **Query Optimization**: N+1 queries avoided; single-batch updates and pessimistic locking used for stock transactions.

## Project Structure

### Documentation (this feature)

```text
specs/005-release-cancel-work-orders/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── contracts/
    └── api.md           # REST API Contract
```

### Source Code Layout

```text
be/src/main/java/fpt/qn/mes/workorder/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── WorkOrderUseCase.java               # Add releaseMaterials and cancelWorkOrder methods
│   │   └── out/
│   │       └── WorkOrderReservationPort.java       # Add releaseReservation method
│   └── service/
│       └── WorkOrderService.java                   # Implement business logic for release & cancel
├── domain/
│   └── repository/
│       └── WorkOrderRepository.java                # Repository interface updates
├── infrastructure/
│   └── persistence/
│       └── adapter/
│           ├── WorkOrderPersistenceAdapter.java    # Work Order status updates
│           └── WorkOrderReservationPersistenceAdapter.java # Stock balance release & movement log
└── presentation/
    └── WorkOrderController.java                    # REST Controllers for release-materials & cancel
```

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Multi-lot historical query | Required by SRS FR-RES-002 to credit stock back to exact original lots | Simply resetting total reserved quantity loses lot-level inventory traceability |
