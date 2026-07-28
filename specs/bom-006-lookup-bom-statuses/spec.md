# Feature Specification: Get BOM Statuses (Lookup)

**Feature Branch**: `feature/bom-006-lookup-bom-statuses`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "Get BOM Statuses (lookup)"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Retrieve BOM Status Lookup List (Priority: P1)

As an Authenticated User (Planner, Manager, Operator, Inspector, Admin), I want to retrieve the master list of all available BOM statuses (`DRAFT`, `ACTIVE`, `INACTIVE`) so that I can populate dropdown filters in the user interface and filter BOMs effectively.

**Why this priority**: Core lookup endpoint required by UI components to render status options dynamically without hardcoding UUIDs or status names.

**Independent Test**: Send `GET /api/boms/statuses` with a valid JWT token; verify HTTP 200 OK response containing an array of lookup entries with `id`, `name`, and `description`.

**Acceptance Scenarios**:

1. **Given** an authenticated user with a valid JWT token, **When** the user sends a `GET /api/boms/statuses` request, **Then** the system queries the `bom_statuses` table and returns HTTP 200 OK with a list of status entries (`id`, `name`, `description`).
2. **Given** an unauthenticated request (no JWT token), **When** a user sends `GET /api/boms/statuses`, **Then** the system rejects the request with HTTP 401 Unauthorized.

---

### Edge Cases

- What happens if the `bom_statuses` database table is empty? Return HTTP 200 OK with an empty array `[]`.
- Does the status lookup list require pagination? No, master lookup lists contain small, fixed sets of rows (typically 3–5 items) and do not use pagination.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide endpoint `GET /api/boms/statuses` accessible to any authenticated user (`@PreAuthorize("isAuthenticated()")`).
- **FR-002**: System MUST query the `bom_statuses` master table using `LookupRepository.findAll("bom_statuses")` (or equivalent domain/infrastructure lookup method).
- **FR-003**: System MUST return HTTP 200 OK with `ApiResponse<List<LookupEntry>>` where each entry contains `id` (UUID), `name` (String), and `description` (String).

### Key Entities *(include if feature involves data)*

- **BOM Status (`bom_statuses`)**: Master lookup table representing lifecycle states of a BOM (`id`, `name`, `description`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Lookup query completes in under 50ms.
- **SC-002**: 100% of authenticated users can access the endpoint regardless of their specific role.
- **SC-003**: Response format matches standard `ApiResponse<List<LookupEntry>>` envelope.

## Assumptions

- `bom_statuses` table is seeded with standard statuses (`DRAFT`, `ACTIVE`, `INACTIVE`) via Flyway migration / database seeders.
- Any logged-in user with a valid JWT token is permitted to read status lookup entries.
