# Feature Specification: Create Bill of Materials Header (Create BOM Header)

**Feature Branch**: `bom-001-create-header`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "create BOM header for module BOM"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a new Bill of Materials header (Priority: P1)

As a Production Planner, I want to create a new Bill of Materials (BOM) header for a manufactured product (Finished Good or Semi-Finished product) so that the system records the initial draft formula version for production planning.

**Why this priority**: Creating the initial BOM header is the prerequisite for adding material items and defining how a product is manufactured. Without a BOM header, work orders and production scheduling cannot take place.

**Independent Test**: Can be tested by creating a BOM header for an existing manufactured product and verifying that the system records the BOM in a Draft state with a unique version number.

**Acceptance Scenarios**:

1. **Given** an active manufactured product (Finished Good or Semi-Finished Good) and an authenticated Planner, **When** the Planner submits a request to create a new BOM with a valid version number, **Then** the system creates the BOM header in `DRAFT` status and assigns the creator identity to the record.
2. **Given** a BOM with version `1` already exists for Product A, **When** the Planner submits a request to create another BOM for Product A with version `1`, **Then** the system rejects the creation request and informs the user of a version conflict.
3. **Given** a product categorized as a Raw Material or Consumable, **When** the Planner attempts to create a BOM for this product, **Then** the system denies the operation and returns an error indicating BOMs are only applicable to manufactured goods.

---

### User Story 2 - Prevent invalid or duplicate BOM creation attempts (Priority: P2)

As a Production Planner, I want the system to validate inputs when creating a BOM so that duplicate versions or invalid target products are prevented before saving.

**Why this priority**: Ensures data integrity in product definitions and prevents broken production plans caused by assigning BOMs to non-manufactured items or duplicating versions.

**Independent Test**: Test by submitting invalid product identifiers or non-positive version numbers and verifying clear validation error responses.

**Acceptance Scenarios**:

1. **Given** a request to create a BOM with a non-existent product identifier, **When** submitted, **Then** the system rejects the request stating the product was not found.
2. **Given** a request to create a BOM with a version number less than 1, **When** submitted, **Then** the system rejects the creation request with an invalid version error message.

---

### Edge Cases

- What happens if the target product status is set to Inactive? The system must reject BOM creation for inactive products.
- What happens if multiple planners attempt to create the same version of a BOM for a product simultaneously? The system must enforce uniqueness so only one creation succeeds, while the other receives a conflict message.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow authorized Planners to create a new Bill of Materials (BOM) for finished goods or semi-finished goods.
- **FR-002**: The system MUST initialize every newly created BOM in `DRAFT` status.
- **FR-003**: The system MUST enforce that the combination of target product and version number is unique across all BOMs.
- **FR-004**: The system MUST record the identity of the user creating the BOM along with the creation timestamp.
- **FR-005**: The system MUST prevent BOM creation for products typed as Raw Materials, Consumables, or Spare Parts.
- **FR-006**: The system MUST prevent BOM creation for target products that are inactive or non-existent.

### Key Entities

- **Bill of Materials (BOM)**: Represents the production recipe/formula header for a manufactured item. Attributes include: Target Product Identifier, Version Number, Status (DRAFT/ACTIVE/INACTIVE), Created By User, and Created Timestamp.
- **Product**: Represents items managed in the factory. Types include Finished Good, Semi-Finished Good, Raw Material, Consumable, and Spare Part.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Planners can successfully record a new draft BOM in under 2 seconds.
- **SC-002**: 100% of invalid creation attempts (duplicate version, invalid product type, non-existent product) are rejected with clear error feedback without corrupting data.
- **SC-003**: 100% of newly created BOM headers correctly default to `DRAFT` status and accurately capture the creator's identity.

## Assumptions

- Users creating BOMs are authenticated and hold the Planner role.
- Product master data already exists prior to BOM creation.
- Detailed material items (components) will be added to the BOM in a subsequent action after the header is created.
