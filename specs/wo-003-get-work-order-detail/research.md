# Technical Research: Get Work Order Detail (`GET /api/work-orders/{id}`)

This document records technical decisions for fetching Work Order detail with embedded materials and event history.

## 1. Single Aggregated Fetch vs Separate Repository Calls

- **Decision**: Fetch `WorkOrder`, `List<WorkOrderMaterial>`, and `List<WorkOrderEvent>` via dedicated repository methods within `@Transactional(readOnly = true)` service method.
- **Rationale**:
  - Keeps jOOQ queries simple and readable (`selectFrom(WORK_ORDERS)`, `selectFrom(WORK_ORDER_MATERIALS)`, `selectFrom(WORK_ORDER_EVENTS)`).
  - Avoids N+1 query overhead by executing exactly 3 indexed primary/foreign key lookups per detail request.
  - Aligns cleanly with Clean Architecture port boundaries without bloating domain entities with ORM-style lazy collections.

## 2. Authorization Role Mapping

- **Decision**: Grant access to `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'OPERATOR', 'FACTORY_MANAGER', 'AUDITOR')")`.
- **Rationale**:
  - `PLANNER` creates and manages orders.
  - `OPERATOR` executes runs and records events.
  - `FACTORY_MANAGER` oversees plant operations.
  - `ADMIN` manages system roles.
  - `AUDITOR` reviews compliance and history events.

## 3. Error Handling Strategy for Non-existent Work Order

- **Decision**: Throw `WorkOrderNotFoundException("Work Order not found with ID: " + id)` extending `AppException(404, ErrorCode.NOT_FOUND, message)`.
- **Rationale**:
  - Automatically handled by `GlobalExceptionHandler` and returns standard `ApiResponse.error(ErrorCode.NOT_FOUND, message)` with HTTP 404.
