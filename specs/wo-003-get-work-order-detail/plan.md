# Implementation Plan: Get Work Order Detail (`GET /api/v1/work-orders/{id}`)

**Branch**: `specs/wo-003-get-work-order-detail`

**Created**: 2026-07-29

**Feature Specification**: [spec.md](spec.md)

---

## Technical Context

- **Tech Stack**: Java 25, Spring Boot 4.1.0, jOOQ 3.21, PostgreSQL 18.
- **Architecture**: Clean Architecture (`presentation` -> `application.port.in` -> `application.service` -> `domain.repository` <- `infrastructure.persistence`).
- **Endpoint**: `GET /api/v1/work-orders/{id}`
- **Security**: `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'OPERATOR', 'FACTORY_MANAGER', 'AUDITOR')")`
- **Output DTO**: `WorkOrderDto` containing embedded `List<WorkOrderMaterialDto> materials` and `List<WorkOrderEventDto> events`.

---

## Constitution Check

- ✅ **Clean Architecture Boundaries**: All domain entities and ports reside in `domain/` and `application/port/in/`. Framework annotations are restricted to `presentation` and `infrastructure`.
- ✅ **No Java Records / Lambdas Only**: Domain objects use standard Java classes with Lombok `@Getter` / `@Builder`. No method references used.
- ✅ **Clean Error Handling**: Non-existent Work Order IDs throw `WorkOrderNotFoundException` mapping to HTTP 404 Not Found.

---

## Proposed Changes

### Domain & Port Layer
- [MODIFY] `be/src/main/java/fpt/qn/mes/workorder/domain/entities/WorkOrder.java`: Add `materials` and `events` fields to domain entity or handle mapping in mapper/service.
- [MODIFY] `be/src/main/java/fpt/qn/mes/workorder/domain/repository/WorkOrderRepository.java`: Add `findMaterialsByWorkOrderId(UUID workOrderId)` and `findEventsByWorkOrderId(UUID workOrderId)`.

### Application Layer
- [MODIFY] `be/src/main/java/fpt/qn/mes/workorder/application/dto/response/WorkOrderDto.java`: Add `List<WorkOrderMaterialDto> materials` and `List<WorkOrderEventDto> events`.
- [NEW] `be/src/main/java/fpt/qn/mes/workorder/application/exception/WorkOrderNotFoundException.java`: Exception throwing HTTP 404 `NOT_FOUND`.
- [MODIFY] `be/src/main/java/fpt/qn/mes/workorder/application/service/WorkOrderService.java`: Implement `getWorkOrderById(UUID id)` fetching WorkOrder, materials, and events, and mapping to `WorkOrderDto`.

### Persistence Layer
- [MODIFY] `be/src/main/java/fpt/qn/mes/workorder/infrastructure/persistence/WorkOrderPersistenceAdapter.java`: Implement `findById(UUID id)`, `findMaterialsByWorkOrderId(UUID id)`, and `findEventsByWorkOrderId(UUID id)` using jOOQ DSLContext.

### Presentation Layer
- [MODIFY] `be/src/main/java/fpt/qn/mes/workorder/presentation/WorkOrderController.java`: Configure `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'OPERATOR', 'FACTORY_MANAGER', 'AUDITOR')")` on `@GetMapping("/{id}")`.

---

## Verification Plan

### Automated Tests
```bash
cd be
./mvnw test "-Dtest=WorkOrderServiceTest,WorkOrderControllerTest"
```

### Manual Verification
Execute scenarios from [quickstart.md](quickstart.md) for valid ID lookup and non-existent ID lookup.
