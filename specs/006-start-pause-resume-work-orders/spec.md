# Feature Specification: Start, Pause, and Resume Work Orders

**Feature Branch**: `006-start-pause-resume-work-orders`

**Created**: 2026-07-30

**Status**: Draft

**Input**: User description: "Implement these endpoints POST /api/v1/work-orders/{id}/start; POST /api/v1/work-orders/{id}/pause; POST /api/v1/work-orders/{id}/resume"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Start Production Run (Priority: P1)

As a Production Operator, I want to start production on a Work Order that has all raw materials reserved, specifying the target machine and optionally the production line, so that the Work Order state transitions to `IN_PROGRESS` and machine status changes to `RUNNING`.

**Why this priority**: Starting production is the fundamental trigger that transitions an order from planning to active execution on the shop floor.

**Independent Test**: Can be fully tested by taking a `READY_TO_PRODUCE` Work Order and an `AVAILABLE` machine, calling `POST /api/v1/work-orders/{id}/start`, and verifying Work Order status changes to `IN_PROGRESS`, Machine status changes to `RUNNING`, a `production_runs` record is created, and a `START` event is recorded in `work_order_events`.

**Acceptance Scenarios**:

1. **Given** a Work Order in `READY_TO_PRODUCE` status and an `AVAILABLE` Machine, **When** the Operator submits a start request with valid `machineId` and optional `productionLineId`, **Then** the Work Order status becomes `IN_PROGRESS`, the Machine status becomes `RUNNING`, a new record in `production_runs` is created with `start_time = now()`, and a `START` work order event is logged.
2. **Given** a Machine currently in `DOWN` or `UNDER_MAINTENANCE` status, **When** the Operator attempts to start a Work Order on this machine, **Then** the system rejects the request with an error indicating the machine is not available.
3. **Given** a Work Order in `DRAFT`, `PLANNED`, or `COMPLETED` status, **When** the Operator attempts to start production, **Then** the system rejects the request with an invalid status transition error.

---

### User Story 2 - Pause and Resume Production (Priority: P1)

As a Production Operator, I want to temporarily pause an in-progress Work Order (e.g. for shift changes, brief component setup, or minor line adjustments) and resume it later without releasing materials or changing machine status, so that shop floor activities are accurately logged.

**Why this priority**: Production halts frequently occur during shop floor execution. Pausing and resuming captures real-time operational context while preserving machine allocations.

**Independent Test**: Can be fully tested by taking an `IN_PROGRESS` Work Order, calling `POST /api/v1/work-orders/{id}/pause` (verifying status changes to `PAUSED` and a `PAUSE` event is logged), and then calling `POST /api/v1/work-orders/{id}/resume` (verifying status reverts to `IN_PROGRESS` and a `RESUME` event is logged).

**Acceptance Scenarios**:

1. **Given** an `IN_PROGRESS` Work Order, **When** the Operator calls `POST /api/v1/work-orders/{id}/pause`, **Then** the Work Order status transitions to `PAUSED`, the Machine status remains `RUNNING`, no stock movements are created, and a `PAUSE` work order event is logged pointing to the active `production_run_id`.
2. **Given** a `PAUSED` Work Order, **When** the Operator calls `POST /api/v1/work-orders/{id}/resume`, **Then** the Work Order status transitions back to `IN_PROGRESS`, the active `production_runs` record continues without modification, and a `RESUME` work order event is logged.
3. **Given** a Work Order that is not currently `IN_PROGRESS`, **When** the Operator attempts to call `pause`, **Then** the system rejects the request with HTTP 400 Bad Request.

---

### User Story 3 - Machine Concurrency Safeguards & Access Control (Priority: P2)

As a System Administrator, I want to enforce that a single machine cannot run more than one Work Order concurrently, and restrict execution endpoints to authorized roles (`OPERATOR`, `PLANNER`, `ADMIN`).

**Why this priority**: Prevents physical and logical machine allocation conflicts when multiple operators control shop floor activities simultaneously.

**Independent Test**: Test starting a second Work Order on a machine that is already `RUNNING` an active order (expecting HTTP 400 rejection).

**Acceptance Scenarios**:

1. **Given** Machine M1 currently running Work Order WO-101, **When** an Operator attempts to start Work Order WO-102 on Machine M1, **Then** the system rejects the request with an error indicating the machine is already running an active work order.
2. **Given** an authenticated user with role `AUDITOR`, **When** the user attempts to call start, pause, or resume endpoints, **Then** the system returns HTTP 403 Forbidden.

---

### Edge Cases

- What happens if `operatorId` is omitted from the start request body? The system defaults `operatorId` to the UUID of the currently authenticated user.
- What happens if the referenced `machineId` does not exist? System returns HTTP 404 Not Found (`RESOURCE_NOT_FOUND`).
- What happens if a machine experiences a breakdown while a Work Order is `IN_PROGRESS`? Machine maintenance tickets handle breakdowns separately; `pause` endpoint remains focused on operational halts.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide endpoint `POST /api/v1/work-orders/{id}/start` accepting required `machineId` with optional `productionLineId` and `operatorId`.
- **FR-002**: System MUST provide endpoint `POST /api/v1/work-orders/{id}/pause` requiring no request body.
- **FR-003**: System MUST provide endpoint `POST /api/v1/work-orders/{id}/resume` requiring no request body.
- **FR-004**: System MUST restrict endpoints to users with role `OPERATOR`, `PLANNER`, or `ADMIN`.
- **FR-005**: System MUST allow `start` only when Work Order is in `READY_TO_PRODUCE` status and target Machine is `AVAILABLE`.
- **FR-006**: System MUST update Work Order status to `IN_PROGRESS` and Machine status to `RUNNING` upon starting production.
- **FR-007**: System MUST insert a new `production_runs` record upon starting production with `start_time = now()`.
- **FR-008**: System MUST allow `pause` only when Work Order is `IN_PROGRESS`, updating status to `PAUSED` while retaining Machine status as `RUNNING`.
- **FR-009**: System MUST allow `resume` only when Work Order is `PAUSED`, updating status to `IN_PROGRESS` while retaining active `production_runs` record.
- **FR-010**: System MUST record corresponding `START`, `PAUSE`, and `RESUME` event logs in `work_order_events` referencing the active `production_run_id`.

### Key Entities *(include if feature involves data)*

- **Work Order**: Represents production order (`id`, `code`, `status`).
- **Production Run**: Tracks single shop floor execution run (`id`, `work_order_id`, `machine_id`, `production_line_id`, `operator_id`, `start_time`, `end_time`).
- **Machine**: Represents shop floor equipment (`id`, `code`, `status`).
- **Work Order Event**: Audit ledger for operational actions (`id`, `work_order_id`, `production_run_id`, `event_type_id`, `operator_id`, `event_timestamp`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Operators can execute start, pause, or resume operations in under 500 milliseconds.
- **SC-002**: 100% of successful start operations correctly create a `production_runs` entry and transition Machine status to `RUNNING`.
- **SC-003**: 100% of pause and resume operations record distinct event logs in `work_order_events` without modifying inventory balances.
- **SC-004**: System prevents 100% of attempts to run multiple Work Orders on the same machine concurrently.

## Assumptions

- Machine status `AVAILABLE` and `RUNNING` exist in master data.
- Event types `START`, `PAUSE`, `RESUME` exist in `work_order_event_types`.
- `production_line_id`, when supplied, belongs to an active production line configured in master data.
