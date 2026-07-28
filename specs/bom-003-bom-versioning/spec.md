# Feature Specification: Bill of Materials Versioning (BOM Versioning)

**Feature Branch**: `feature/bom-003-bom-versioning`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "BOM Versioning functionality for module BOM according to SRS FR-BOM-003"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a New BOM Version from Existing BOM (Priority: P1)

As a Production Planner, I want to create a new draft BOM version cloned from an existing BOM (whether ACTIVE or INACTIVE) so that I can modify component recipes without editing historical production specifications directly.

**Why this priority**: SRS FR-BOM-003 strictly mandates that BOMs used in production/work orders cannot be modified directly. Creating a new version from an existing BOM is the required business mechanism for recipe evolution and continuous improvement.

**Independent Test**: Trigger the "Create New Version" action (`POST /api/boms/{id}/new-version`) on an existing BOM and verify that a new `DRAFT` BOM is created with `version = maxVersion + 1` and all component line items copied.

**Acceptance Scenarios**:

1. **Given** an existing BOM (Version 1) for Product A with 3 component items, **When** the Planner requests to create a new version from Version 1, **Then** the system creates a new BOM header in `DRAFT` status with `version = 2` for Product A and copies all 3 component items (`materialProductId`, `quantityPerUnit`, `scrapRate`) to the new BOM.
2. **Given** Product A already has Version 1 (`ACTIVE`) and Version 2 (`DRAFT`), **When** the Planner creates a new version from Version 1, **Then** the system automatically calculates `version = 3` (next highest version number for Product A).
3. **Given** a non-existent source BOM ID, **When** a user submits a create new version request, **Then** the system rejects the operation with a 404 Not Found error.

---

### User Story 2 - Prevent direct editing of ACTIVE or INACTIVE BOMs (Priority: P2)

As a Production Planner, I want the system to block direct modifications (adding, updating, or deleting items) on `ACTIVE` or `INACTIVE` BOMs so that historical production formulas remain immutable for audit and traceability.

**Why this priority**: Ensures zero data corruption or unapproved recipe changes on active or historical production standards.

**Independent Test**: Attempt to add or delete items on a BOM that is in `ACTIVE` or `INACTIVE` status and verify that the system returns a 400 Bad Request error.

**Acceptance Scenarios**:

1. **Given** a BOM in `ACTIVE` status, **When** a user attempts to add a component item to it, **Then** the system denies the modification and instructs the user to create a new draft version first.
2. **Given** a BOM in `INACTIVE` status, **When** a user attempts to delete a component item from it, **Then** the system denies the operation enforcing immutability.

---

### Edge Cases

- What happens if a source BOM has 0 items when creating a new version? The system will create a new draft header version with 0 items.
- What happens if the target finished product is inactive when attempting to create a new version? The system must reject version creation for inactive products.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow authorized Planners to create a new BOM version cloned from an existing BOM header.
- **FR-002**: The system MUST automatically assign `version = max(existing_versions_for_product) + 1` to the newly created version.
- **FR-003**: The system MUST initialize the newly created BOM version in `DRAFT` status.
- **FR-004**: The system MUST deep-copy all component items (`materialProductId`, `quantityPerUnit`, `scrapRate`) from the source BOM to the new draft BOM version.
- **FR-005**: The system MUST prevent direct modification (adding, updating, or deleting items) on any BOM that is not in `DRAFT` status (`ACTIVE` or `INACTIVE`).
- **FR-006**: The system MUST record the identity of the user creating the new version along with the creation timestamp.

### Key Entities

- **Bill of Materials (BOM)**: Header entity with `finishedProductId`, `version`, and `bomStatusId`.
- **BOM Item**: Component lines belonging to a BOM.
- **BOM Status**: `DRAFT` (editable), `ACTIVE` (immutable production standard), `INACTIVE` (immutable historical archive).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Planners can create a new version from a complex 20-item BOM in under 1 second with all line items accurately copied.
- **SC-002**: 100% of modification attempts on `ACTIVE` or `INACTIVE` BOMs are rejected with clear error feedback.
- **SC-003**: System guarantees auto-incremented version numbers without version collision per product.

## Assumptions

- Users creating new versions hold the `PLANNER` or `ADMIN` role.
- Source BOM must exist and belong to an active product.
- Newly cloned version remains in `DRAFT` status until explicitly activated via `POST /api/boms/{id}/activate`.
