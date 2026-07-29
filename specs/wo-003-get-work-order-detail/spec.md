# Feature Specification: Get Work Order Detail (`GET /api/v1/work-orders/{id}`)

**Feature Branch**: `specs/wo-003-get-work-order-detail`

**Created**: 2026-07-29

**Status**: Draft

**Input**: User description: "Implement endpoint GET /api/v1/work-orders/{id}"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Get Detailed Work Order Information with Embedded Materials and Events (Priority: P1) 🎯 MVP

As an Authorized User (`ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, `AUDITOR`), I want to query a specific Work Order by its unique ID so that I can view its complete state, including basic metadata, material requirements (`materials`), and history events (`events`).

**Why this priority**: Retrieving complete Work Order details in a single query provides essential operational visibility for planners, operators, and plant managers without requiring multiple round-trip API requests.

**Independent Test**: Can be tested by issuing `GET /api/v1/work-orders/{id}` with a valid Work Order ID and verifying that the returned HTTP 200 response contains basic details, the list of required/reserved/consumed materials, and history events (START, PAUSE, RESUME, COMPLETE).

**Acceptance Scenarios**:

1. **Given** an existing Work Order ID, **When** an authorized user sends `GET /api/v1/work-orders/{id}`, **Then** system returns HTTP 200 OK containing `WorkOrderDto` with `materials` list and `events` list.
2. **Given** a valid JWT token for an authorized role (`ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, `AUDITOR`), **When** calling `GET /api/v1/work-orders/{id}`, **Then** access is granted.

---

### User Story 2 - Work Order Not Found Handling (Priority: P2)

As an Authorized User, when I query a Work Order ID that does not exist in the database, I want to receive a clear error response so that I know the requested resource was not found.

**Why this priority**: Clear error handling prevents client application confusion when attempting to access invalid or deleted Work Orders.

**Independent Test**: Issue `GET /api/v1/work-orders/{non-existent-uuid}` and verify HTTP 404 response with `NOT_FOUND` error code.

**Acceptance Scenarios**:

1. **Given** a non-existent Work Order UUID, **When** calling `GET /api/v1/work-orders/{id}`, **Then** system returns HTTP 404 Not Found with error code `NOT_FOUND`.

---

### User Story 3 - Role-Based Access Control Enforcement (Priority: P3)

As a System Security Guard, I want to reject requests from unauthorized roles (or unauthenticated requests) so that sensitive manufacturing execution details are protected.

**Why this priority**: Ensures system security and compliance with enterprise authorization rules.

**Independent Test**: Issue request without JWT or with unauthorized role token and verify HTTP 401 / HTTP 403 response.

**Acceptance Scenarios**:

1. **Given** an unauthenticated request or unauthorized role token, **When** calling `GET /api/v1/work-orders/{id}`, **Then** system returns HTTP 401 Unauthorized or HTTP 403 Forbidden.

---

### Edge Cases

- What happens when an invalid (non-UUID format) ID is provided in the URL? System returns HTTP 400 Bad Request (`INVALID_INPUT`).
- How does the system handle Work Orders with zero materials or zero events? System returns empty arrays `[]` for `materials` and `events`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-WO-005**: System MUST provide REST endpoint `GET /api/v1/work-orders/{id}` returning detailed Work Order information.
- **FR-WO-006**: System MUST restrict access to `GET /api/v1/work-orders/{id}` to roles `ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, and `AUDITOR`.
- **FR-WO-007**: System MUST embed the list of associated material requirements (`materials` containing `materialProductId`, `requiredQuantity`, `reservedQuantity`, `consumedQuantity`) inside the returned `WorkOrderDto`.
- **FR-WO-008**: System MUST embed the list of associated history events (`events` containing `eventTypeId`, `operatorId` as performer, `eventTimestamp` as time, `actualQuantity`, `goodQuantity`, `defectQuantity`, `scrapQuantity`, `note`) inside the returned `WorkOrderDto`.
- **FR-WO-009**: System MUST return HTTP 404 Not Found with error code `NOT_FOUND` when the specified Work Order ID does not exist in the database.

### Key Entities *(include if feature involves data)*

- **WorkOrder**: Core manufacturing entity representing a production order.
- **WorkOrderMaterial**: Material requirements associated with a Work Order (`requiredQuantity`, `reservedQuantity`, `consumedQuantity`).
- **WorkOrderEvent**: History events associated with a Work Order (e.g. `START`, `PAUSE`, `RESUME`, `COMPLETE`) with timestamps and performer (`operatorId`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of valid `GET /api/v1/work-orders/{id}` requests return HTTP 200 OK with complete details, embedded `materials`, and embedded `events`.
- **SC-002**: 100% of non-existent ID queries return HTTP 404 Not Found with error code `NOT_FOUND`.
- **SC-003**: 100% of requests from unauthorized roles are rejected with HTTP 403 Forbidden.

## Assumptions

- Material requirements and event history are fetched within a single database transaction or aggregated query to maintain consistency.
- Roles `ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, and `AUDITOR` have read access to Work Order details.
