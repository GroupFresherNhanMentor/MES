# Implementation Plan: Update Work Order

**Branch**: `feat/POST-GET-WO-final` | **Date**: 2026-07-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/wo-004-update-work-order/spec.md`

## Summary

Implement the `PUT /api/work-orders/{id}` endpoint allowing ADMIN and PLANNER roles to update planning fields (code, quantity, dates, priority) and perform simple status transitions (DRAFT ⇄ PLANNED). The endpoint enforces strict validation (positive quantity, valid date range, unique code), rejects updates on Work Orders not in DRAFT/PLANNED status, and automatically recalculates material requirements when planned quantity changes.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21, MapStruct, Lombok

**Storage**: PostgreSQL 18 (via jOOQ DSLContext)

**Testing**: JUnit 5 + Mockito (unit), Spring Boot Test + Testcontainers (integration)

**Target Platform**: Linux server / Docker container

**Project Type**: Web service (REST API backend)

**Performance Goals**: Standard web API latency (< 500ms p95)

**Constraints**: Clean Architecture layers, no JPA/EntityManager, no method references

**Scale/Scope**: Single endpoint with 6 user stories and 10 functional requirements

## Constitution Check

| Gate | Status | Notes |
|------|--------|-------|
| Clean Architecture 4-layer structure | ✅ PASS | All changes follow presentation → application → domain ← infrastructure |
| jOOQ Only for DB access | ✅ PASS | New queries use DSLContext, no JPA |
| MapStruct Only for mapping | ✅ PASS | Existing WorkOrderDtoMapper reused |
| Exceptions from Service Layer | ✅ PASS | New `InvalidWorkOrderStateException` in application/exception/ |
| Consistent API Responses | ✅ PASS | Returns `ResponseEntity<ApiResponse<WorkOrderDto>>` |
| Tests Are Mandatory | ✅ PASS | Unit tests for service, controller slice tests planned |

## Project Structure

### Documentation (this feature)

```text
specs/wo-004-update-work-order/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── put-work-order.json
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (repository root)

```text
be/src/main/java/fpt/qn/mes/
├── common/exception/
│   └── ErrorCode.java                          # [MODIFY] Add INVALID_STATUS_TRANSITION
├── workorder/
│   ├── application/
│   │   ├── dto/request/
│   │   │   └── UpdateWorkOrderRequest.java     # [MODIFY] Add @Setter, validation annotations
│   │   ├── exception/
│   │   │   └── InvalidWorkOrderStateException.java   # [NEW]
│   │   ├── port/in/
│   │   │   └── WorkOrderUseCase.java           # (no changes needed)
│   │   └── service/
│   │       └── WorkOrderService.java           # [MODIFY] Implement updateWorkOrder()
│   ├── domain/
│   │   └── repository/
│   │       └── WorkOrderRepository.java        # [MODIFY] Add findStatusNameById(), existsByCodeAndIdNot(), updateMaterial()
│   ├── infrastructure/
│   │   └── persistence/
│   │       └── WorkOrderPersistenceAdapter.java # [MODIFY] Implement new repository methods
│   └── presentation/
│       └── WorkOrderController.java            # [MODIFY] Add @PreAuthorize, remove stub

be/src/test/java/fpt/qn/mes/workorder/
├── application/service/
│   └── WorkOrderServiceTest.java               # [MODIFY] Add update tests
└── presentation/
    └── WorkOrderControllerTest.java            # [MODIFY] Add update tests

be/src/main/resources/seed/
└── work-order-status-transitions.json          # [MODIFY] Add PLANNED → DRAFT transition
```

**Structure Decision**: All changes fit within the existing `workorder` module following the established Clean Architecture pattern. No new modules or packages required.
