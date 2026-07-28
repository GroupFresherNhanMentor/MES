# Feature Specification: Activate Bill of Materials (Activate BOM)

**Feature Branch**: `feature/bom-002-activate-bom`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "Activate BOM functionality for module BOM according to SRS FR-BOM-002"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Activate a Draft BOM (Priority: P1)

As a Production Planner, I want to activate a draft Bill of Materials (BOM) for a manufactured product so that it becomes the official active recipe used by Work Orders for material reservation and production execution.

**Why this priority**: Activating a BOM is a critical milestone in manufacturing operations. Work Orders can only be created from an active BOM. Without the ability to activate a BOM, production planning cannot proceed to execution.

**Independent Test**: Can be tested by selecting a draft BOM that has at least one component item and triggering the activate action, verifying that its status changes to `ACTIVE` and any previously active BOM for that product transitions to `INACTIVE`.

**Acceptance Scenarios**:

1. **Given** a draft BOM with at least 1 component item and an authenticated Planner, **When** the Planner requests to activate the BOM, **Then** the system transitions the target BOM status to `ACTIVE`.
2. **Given** Product A currently has BOM Version 1 in `ACTIVE` status and BOM Version 2 in `DRAFT` status (with items), **When** the Planner activates Version 2, **Then** the system automatically transitions Version 1 to `INACTIVE` and sets Version 2 to `ACTIVE`, ensuring exactly one active BOM version exists for Product A.
3. **Given** a draft BOM with 0 component items, **When** the Planner attempts to activate it, **Then** the system rejects the operation and displays an error message stating that an empty BOM cannot be activated.

---

### User Story 2 - Prevent invalid activation state transitions (Priority: P2)

As a Production Planner, I want the system to reject invalid activation requests (such as re-activating an already active or inactive BOM) so that the lifecycle state of BOMs remains consistent and predictable.

**Why this priority**: Prevents redundant state updates and safeguards historical BOM versions from accidental re-activation without proper version control.

**Independent Test**: Test by sending activation requests for BOMs that are already `ACTIVE` or `INACTIVE` and checking that appropriate error feedback is returned.

**Acceptance Scenarios**:

1. **Given** a BOM already in `ACTIVE` status, **When** a user submits an activation request for it, **Then** the system rejects the request indicating the BOM is already active.
2. **Given** a historical BOM in `INACTIVE` status, **When** a user submits an activation request for it, **Then** the system rejects the request, enforcing that only `DRAFT` BOMs can be activated (new changes require creating a new draft version).

---

### Edge Cases

- What happens if two Planners attempt to activate different draft versions of the same product simultaneously? The system must process activations in a transaction so that atomic state transition guarantees exactly one `ACTIVE` version remains.
- What happens if a draft BOM contains invalid or inactive material components at activation time? The system should validate that component material products exist and are active before allowing activation.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow authorized Planners to activate a BOM currently in `DRAFT` status.
- **FR-002**: The system MUST verify that a draft BOM contains at least 1 component item before allowing activation.
- **FR-003**: The system MUST enforce that exactly one BOM version is in `ACTIVE` status per manufactured product at any given time.
- **FR-004**: When activating a target draft BOM, the system MUST automatically transition any existing `ACTIVE` BOM for that product to `INACTIVE` within the same transaction.
- **FR-005**: The system MUST reject activation requests for BOMs that are not currently in `DRAFT` status (e.g. already `ACTIVE` or `INACTIVE`).
- **FR-006**: The system MUST prevent activation of a BOM if the target product or any of its component materials are inactive or deleted.

### Key Entities

- **Bill of Materials (BOM)**: Header entity containing target product, version, and status (`DRAFT`, `ACTIVE`, `INACTIVE`).
- **BOM Item**: Component line items linking the BOM to material products with quantities and scrap rates.
- **BOM Status**: Lookup entity defining states: `DRAFT` (initial), `ACTIVE` (current production standard), `INACTIVE` (superseded version).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Planners can activate a valid draft BOM with instant status transition (< 1 second).
- **SC-002**: 100% of activation attempts on empty draft BOMs are rejected with clear user feedback.
- **SC-003**: System guarantees 100% single-active-BOM consistency per product (zero instances of multiple active BOMs for a single product).

## Assumptions

- Target product and component materials exist and are active in master data.
- Users triggering activation hold the `PLANNER` or `ADMIN` role.
- Work Orders created after activation will reference the newly activated BOM version.
