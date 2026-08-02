# Implementation Plan: Start, Pause, and Resume Work Orders

**Branch**: `006-start-pause-resume-work-orders` | **Date**: 2026-07-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-start-pause-resume-work-orders/spec.md`

## Summary

Implement backend endpoints `POST /api/work-orders/{id}/start`, `POST /api/work-orders/{id}/pause`, and `POST /api/work-orders/{id}/resume` in the Spring Boot 4-layer Clean Architecture.
The implementation will handle Work Order status transitions (`READY_TO_PRODUCE` → `IN_PROGRESS` ⇄ `PAUSED`), Machine status updates (`AVAILABLE` → `RUNNING`), production run tracking in `production_runs` (`start_time = now()`), event logging in `work_order_events`, and machine concurrency safeguards using database pessimistic locking (`SELECT FOR UPDATE`).

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
- [x] **Database Schema**: Existing tables `work_orders`, `production_runs`, `work_order_events`, `machines` utilized without modifying Flyway migrations.
- [x] **Security Constraints**: Endpoints secured with `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'OPERATOR')")`.
- [x] **Query Optimization**: N+1 queries avoided; pessimistic locking used for machine status updates and production run creation.

## Project Structure

### Documentation (this feature)

```text
specs/006-start-pause-resume-work-orders/
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
│   ├── dto/
│   │   └── request/
│   │       └── StartWorkOrderRequest.java          # Request body for /start endpoint
│   ├── port/
│   │   ├── in/
│   │   │   └── WorkOrderUseCase.java               # Add startWorkOrder, pauseWorkOrder, resumeWorkOrder
│   │   └── out/
│   │       └── ProductionRunPort.java              # Output port for production_runs & machine status
│   └── service/
│       └── WorkOrderService.java                   # Implement business logic for start, pause, resume
├── domain/
│   ├── entities/
│   │   └── ProductionRun.java                      # Production run domain entity
│   └── repository/
│       └── ProductionRunRepository.java            # Repository interface for production runs
├── infrastructure/
│   └── persistence/
│       └── adapter/
│           ├── WorkOrderPersistenceAdapter.java    # Status updates and event logging
│           └── ProductionRunPersistenceAdapter.java # jOOQ implementation for production runs & machines
└── presentation/
    └── WorkOrderController.java                    # REST Controllers for /start, /pause, /resume
```

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Separate `production_runs` table | Required by URS & SRS to snapshot execution runs separately from events ledger | Merging events and runs into a single table violates audit trail history requirements |
