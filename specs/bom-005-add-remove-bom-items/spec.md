# Feature Specification: Add and Remove BOM Items

**Feature Branch**: `feature/bom-005-add-remove-bom-items`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "Add BOM Item / Remove BOM Item"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Add Component Item to DRAFT BOM (Priority: P1)

As a Planner or Administrator, I want to add a raw material or semi-finished component item to an existing DRAFT Bill of Materials (BOM) so that I can define the composition required for manufacturing the product.

**Why this priority**: Core capability needed to construct BOM component structures. Without items, a BOM cannot be activated or used for Work Orders.

**Independent Test**: Can be tested by sending `POST /api/boms/{bomId}/items` with valid material product ID, quantity, unit, and scrap rate on a DRAFT BOM, verifying HTTP 201 response with created item details.

**Acceptance Scenarios**:

1. **Given** a BOM in `DRAFT` status and a valid material product ID, **When** the user submits an item with quantity = 2.50, unit = "PCS", and scrap rate = 0.02, **Then** the system creates the BOM item record, links it to the BOM header, and returns HTTP 201 Created with full item DTO.
2. **Given** a BOM in `ACTIVE` or `INACTIVE` status, **When** the user attempts to add a component item, **Then** the system rejects the request with HTTP 400 Bad Request ("Only DRAFT BOMs can be modified; create a new version instead").
3. **Given** a DRAFT BOM, **When** the user submits an item with a non-existent `materialProductId` or invalid `quantityPerUnit` (<= 0), **Then** the system returns HTTP 400 Bad Request.

---

### User Story 2 - Remove Component Item from DRAFT BOM (Priority: P1)

As a Planner or Administrator, I want to remove a component item from a DRAFT BOM so that I can correct errors or revise the material structure before activating the BOM version.

**Why this priority**: Essential editing capability to clean up unintended or incorrect component entries prior to activation.

**Independent Test**: Can be tested by sending `DELETE /api/boms/{bomId}/items/{itemId}` on a DRAFT BOM, verifying HTTP 204 No Content response and confirming item is removed from the BOM detail response.

**Acceptance Scenarios**:

1. **Given** a BOM in `DRAFT` status containing a component item, **When** the user sends a delete request for that item ID, **Then** the system deletes the item record from the database and returns HTTP 204 No Content.
2. **Given** a BOM in `ACTIVE` or `INACTIVE` status containing items, **When** the user attempts to delete a component item, **Then** the system rejects the request with HTTP 400 Bad Request ("Only DRAFT BOMs can be modified; create a new version instead").
3. **Given** a DRAFT BOM, **When** the user attempts to delete a non-existent item ID or an item belonging to a different BOM, **Then** the system returns HTTP 404 Not Found.

---

### Edge Cases

- What happens when a user attempts to add/remove an item on a BOM that is currently being activated concurrently?
- How does the system handle deleting an item from a BOM that leaves the BOM with 0 items? (Allowed in DRAFT, but activation will prevent activating empty BOMs).
- What happens if `scrapRate` is omitted in the request? Default to `0.00`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide endpoint `POST /api/boms/{bomId}/items` allowing users with role `ADMIN` or `PLANNER` to add component items to a BOM.
- **FR-002**: System MUST validate that the target BOM exists and is in `DRAFT` status before adding an item; otherwise return HTTP 400 Bad Request.
- **FR-003**: System MUST validate that `materialProductId` exists in the product catalog and `quantityPerUnit` is greater than zero (`> 0`).
- **FR-004**: System MUST provide endpoint `DELETE /api/boms/{bomId}/items/{itemId}` allowing users with role `ADMIN` or `PLANNER` to remove component items from a BOM.
- **FR-005**: System MUST validate that the target BOM exists and is in `DRAFT` status before removing an item; otherwise return HTTP 400 Bad Request.
- **FR-006**: System MUST return HTTP 201 Created for successful item additions and HTTP 204 No Content for successful item removals.

### Key Entities *(include if feature involves data)*

- **BOM Header (`boms`)**: Represents the Bill of Materials header, containing `id`, `finishedProductId`, `version`, `bomStatusId`, `createdBy`, `createdAt`.
- **BOM Item (`bom_items`)**: Represents an individual component line item, containing `id`, `bomId`, `materialProductId`, `quantityPerUnit`, `unit`, `scrapRate`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Adding a component item to a DRAFT BOM completes in under 100ms.
- **SC-002**: 100% of modification attempts on non-DRAFT BOMs (`ACTIVE`, `INACTIVE`) are strictly blocked with informative HTTP 400 error messages.
- **SC-003**: 100% of valid item removal requests return HTTP 204 and permanently dissociate the item from the BOM.

## Assumptions

- Users adding/deleting BOM items have valid JWT tokens with `ADMIN` or `PLANNER` role.
- Component materials refer to valid product records existing in the `products` table.
- BOM item modifications are allowed freely while the BOM is in `DRAFT` status.
