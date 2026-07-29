# Feature Specification: Query Work Orders (`GET /api/v1/work-orders`)

**Feature Branch**: `wo-001-query-work-orders`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "Implement GET /api/v1/work-orders endpoint"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Paginated Work Order Search & Filter (Priority: P1)

As a Production Planner, Operator, Factory Manager, or Auditor, I want to query the list of Work Orders with optional filters (status, product, code search) and pagination support, so that I can monitor plant production schedules and track ongoing manufacturing operations.

**Why this priority**: Core production visibility requirement. Planners and Operators must be able to view and search work orders to manage factory floor execution.

**Independent Test**: Can be tested independently by issuing search requests with various filter combinations (by status ID, finished product ID, fuzzy code string) and verifying matching work order records and pagination metadata.

**Acceptance Scenarios**:

1. **Given** multiple Work Orders exist in various statuses, **When** an authorized user requests the list without parameters, **Then** the system returns a paginated list sorted by creation timestamp descending using default page size (20).
2. **Given** Work Orders associated with different finished products, **When** a user filters by a specific `finishedProductId`, **Then** only Work Orders for that product are returned.
3. **Given** Work Orders in different statuses (`DRAFT`, `PLANNED`, `IN_PROGRESS`), **When** a user filters by `statusId`, **Then** only Work Orders matching that exact status are returned.
4. **Given** a Work Order code "WO-2026-0089", **When** a user searches with `code=0089`, **Then** the system performs case-insensitive fuzzy matching and includes "WO-2026-0089" in results.

---

### User Story 2 - Role-Based Query Authorization (Priority: P2)

As a System Administrator, I want the query endpoint to enforce strict role-based access control (RBAC), so that only authorized factory personnel (`ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, `AUDITOR`) can retrieve work order data.

**Why this priority**: Security requirement to protect manufacturing operational data from unauthorized access.

**Independent Test**: Can be tested independently by calling the endpoint with valid tokens of authorized roles vs unauthorized roles vs unauthenticated requests.

**Acceptance Scenarios**:

1. **Given** an authenticated user with role `PLANNER`, `OPERATOR`, `ADMIN`, `FACTORY_MANAGER`, or `AUDITOR`, **When** querying work orders, **Then** the request succeeds with HTTP 200.
2. **Given** an authenticated user with an unauthorized role (e.g. external guest), **When** querying work orders, **Then** access is denied with HTTP 403 Forbidden.
3. **Given** an unauthenticated request without a token, **When** querying work orders, **Then** access is rejected with HTTP 401 Unauthorized.

---

### User Story 3 - Pagination & Large Dataset Retrieval (Priority: P3)

As a Factory Manager or System Auditor, I want the system to handle large volumes of work orders efficiently using 0-based pagination, so that system responsiveness is maintained even with thousands of historical work orders.

**Why this priority**: Performance and scalability assurance for high-volume manufacturing environments.

**Independent Test**: Can be tested by requesting custom `page` and `size` parameters and verifying `totalElements` and `totalPages` metadata.

**Acceptance Scenarios**:

1. **Given** 100 Work Orders in the system, **When** requesting `page=1` and `size=15`, **Then** records 16 through 30 are returned alongside `totalElements=100` and `totalPages=7`.

---

### Edge Cases

- What happens when a search query yields 0 matching Work Orders? System returns HTTP 200 with an empty content list and `totalElements=0`.
- How does system handle invalid UUID strings in `finishedProductId` or `statusId` query params? System rejects request with HTTP 400 Bad Request detailing validation failure.
- What happens when `page` or `size` are negative values? System defaults or validates to non-negative parameters.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a paginated search capability for Work Orders returning core header attributes (ID, code, finished product ID, BOM ID, planned quantity, planned start/end dates, priority ID, status ID, created by, created at).
- **FR-002**: System MUST support optional filtering by `finishedProductId`, `statusId`, and partial case-insensitive `code` search.
- **FR-003**: System MUST enforce 0-based page numbering with configurable page size (defaulting to 20 items per page).
- **FR-004**: System MUST sort retrieved Work Orders by creation timestamp in descending order by default.
- **FR-005**: System MUST restrict access to authorized roles: `ADMIN`, `PLANNER`, `OPERATOR`, `FACTORY_MANAGER`, and `AUDITOR`.
- **FR-006**: System MUST return all successful responses wrapped in a standard API envelope containing `success`, `message`, `data`, and pagination metadata (`page`, `size`, `totalElements`, `totalPages`).

### Key Entities *(include if feature involves data)*

- **WorkOrder**: Core manufacturing entity representing a production run request for a specific quantity of a finished product.
- **WorkOrderStatus**: Reference lookup entity representing lifecycle state (`DRAFT`, `PLANNED`, `MATERIAL_SHORTAGE`, `READY_TO_PRODUCE`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`, `CANCELLED`).
- **Product**: Master data entity representing the manufactured item.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Authorized users can search and retrieve Work Order lists with sub-500ms response times for datasets up to 100,000 records.
- **SC-002**: 100% of unauthorized access attempts (missing token or invalid role) are blocked with appropriate HTTP status codes (401/403).
- **SC-003**: Search results accurately match all combined filter criteria (100% filter precision).

## Assumptions

- Standard JWT authentication mechanism is enabled and populated with user roles.
- Work Order database records and reference lookup tables (`work_order_statuses`) are seeded in the database.
