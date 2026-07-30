# Phase 0 Research: Start, Pause, and Resume Work Orders

## Research Topic 1: Machine Status & Concurrency Lifecycle

### Problem Statement
How should machine status transitions and concurrency locks be handled during `start`, `pause`, and `resume`?

### Decision
- **Start (`/start`)**:
  1. Verify target Machine status is `AVAILABLE` (not `DOWN` or `UNDER_MAINTENANCE`).
  2. Verify no active `production_runs` exist for `machine_id` where `end_time IS NULL`.
  3. Update Machine status to `RUNNING`.
- **Pause (`/pause`)**:
  1. Retain Machine status as `RUNNING` (no machine status change).
  2. Retain active `production_runs` record without updating `end_time`.
- **Resume (`/resume`)**:
  1. Retain Machine status as `RUNNING`.
  2. Retain active `production_runs` record.

### Rationale
- Complies with SRS section 3.8 (FR-PROD-001, FR-PROD-002).
- Prevents 2 Work Orders from running on the same machine simultaneously while keeping operational halts lightweight.

---

## Research Topic 2: Database Schema & Entity Separation

### Problem Statement
Should `production_runs` and `work_order_events` be separated?

### Decision
Yes, follow SRS specification:
- `production_runs`: Snapshot table tracking single execution run (`machine_id`, `production_line_id`, `operator_id`, `start_time`, `end_time`, `actual_quantity`).
  - INSERT on `start`.
  - UPDATE on `complete`.
- `work_order_events`: Fine-grained audit log ledger (`event_type_id`, `work_order_id`, `production_run_id`, `operator_id`, `event_timestamp`).
  - INSERT on `start`, `pause`, `resume`, `complete`.

---

## Research Topic 3: REST Payload & DTO Structure

### Decision
- `POST /api/v1/work-orders/{id}/start` -> Accepts `StartWorkOrderRequest` body (`machineId`, `productionLineId`, optional `operatorId`). Returns `ApiResponse<WorkOrderDto>`.
- `POST /api/v1/work-orders/{id}/pause` -> No request body. Returns `ApiResponse<WorkOrderDto>`.
- `POST /api/v1/work-orders/{id}/resume` -> No request body. Returns `ApiResponse<WorkOrderDto>`.
