# Implementation Plan: Complete Work Order

**Branch**: `feature/complete-wo` | **Date**: 2026-08-01 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification for `POST /api/v1/work-orders/{id}/complete`.

## Summary

Implement the Operator-only completion action for an in-progress Work Order. The action validates reported output quantities and output destination, consumes raw-material reservations proportionally to actual output, allocates material scrap per original reserved lot, releases unused reservations, creates independently traceable good and defective output in quality isolation, creates pending QC inspections, closes the production run, releases the machine, transitions the Work Order through the configured active transition, and writes a completion audit entry. All effects run in one application transaction through workorder-owned persistence ports.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.1.0, Spring Security JWT, jOOQ 3.21, MapStruct 1.6.3, Lombok

**Storage**: PostgreSQL 18; Flyway schema migrations; generated jOOQ tables

**Testing**: JUnit 5, Mockito, AssertJ, Spring Boot integration tests with Testcontainers

**Target Platform**: Containerized backend service on Windows/Linux development environments

**Project Type**: REST web service in a modular monolith

**Performance Goals**: Valid completion returns within 1 second under normal load; two concurrent requests for the same Work Order permit exactly one completion.

**Constraints**: Only `OPERATOR` is authorized; all mutable production, stock, quality, and audit effects are atomic; no stock balance may become negative; output warehouse and location must be valid; all physical movements retain lot-level traceability.

**Scale/Scope**: One completion action per Work Order, one active production run, multiple Work Order material lines and original reservation lots, and exactly two classified finished-goods lots per completion.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Evidence |
|------|--------|----------|
| Clean Architecture dependency flow | Pass | Controller depends on `WorkOrderUseCase`; service uses repositories and workorder-owned output ports; jOOQ remains in infrastructure adapters. |
| jOOQ-only database access | Pass | New completion persistence adapter uses `DSLContext`; schema change is a Flyway migration. |
| Mapper separation | Pass | Existing DTO mapping remains MapStruct; any new jOOQ mapping stays in infrastructure. |
| Service-layer exceptions and transactions | Pass | Validation, transition rejection, and transaction boundary belong to `WorkOrderService`; adapters report persistence outcomes only. |
| API response convention | Pass | Endpoint returns the existing `ApiResponse` envelope. |
| Mandatory tests | Pass | Unit, controller, integration, atomicity, and concurrent-completion test tasks are required. |

**Post-design re-check**: Pass. The proposed workorder-owned persistence port prevents a reverse dependency from `workorder` to `quality`, which would violate the module DAG.

## Project Structure

### Documentation (this feature)

```text
specs/007-complete-work-order/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)
```text
be/
├── src/main/java/fpt/qn/mes/
│   ├── workorder/
│   │   ├── application/dto/workorder/complete/
│   │   ├── application/port/in/
│   │   ├── application/port/out/
│   │   ├── application/service/
│   │   ├── infrastructure/persistence/adapter/
│   │   └── presentation/
│   └── quality/application/dto/inspection/create/
├── src/main/resources/db/migration/
└── src/test/java/fpt/qn/mes/workorder/
    ├── application/service/
    ├── presentation/
    └── integration/
```

**Structure Decision**: Extend the existing `workorder` module. The service remains the orchestration boundary and uses a new workorder-owned completion output port, implemented by a jOOQ adapter, to atomically persist stock, lot, QC, run, machine, event, and audit changes. No new module or frontend is required.

## Complexity Tracking

No constitution violations require justification.
