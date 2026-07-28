# Feature Specification: Master Data Management

**Feature Branch**: `20260728114047-master-data-management`

**Created**: 2026-07-28

**Status**: Draft

**Input**: User description: "Implement Master Data Management for FactoryFlow — Product, Warehouse, Location, Machine, Production Line CRUD with business rules"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Admin manages products (Priority: P1)

The factory has thousands of materials and finished goods. The Admin needs to create, update, list, and deactivate products so that production and inventory teams can reference accurate product data. Products are classified into types (raw material, semi-finished, finished good, consumable, spare part). Product codes must be unique across the system. Products are never hard-deleted — only deactivated.

**Why this priority**: Product is the central entity. Every other module (Inventory, BOM, Work Order) depends on product data. Without this, the factory cannot operate.

**Independent Test**: Can be fully tested by creating a product with a unique code and confirming the system returns the created product. Then attempting to create another product with the same code and confirming the system rejects it with a duplicate-code error.

**Acceptance Scenarios**:

1. **Given** no product with code "SP-001" exists, **When** the Admin submits a product with code "SP-001", **Then** the system creates the product and returns its details with status ACTIVE
2. **Given** a product with code "SP-001" already exists, **When** the Admin submits a product with code "SP-001", **Then** the system rejects with a duplicate-code error
3. **Given** a product has been referenced in stock movements, **When** the Admin attempts to deactivate it, **Then** the system rejects the deactivation
4. **Given** a product has no stock movements, **When** the Admin deactivates it, **Then** the product status changes to INACTIVE and it is no longer available for new transactions

---

### User Story 2 - Admin manages warehouses and locations (Priority: P2)

The factory operates multiple physical warehouses (raw materials, finished goods, WIP). Each warehouse has named/shelved locations. The Admin needs to define warehouses and their locations so that inventory can be stored and tracked precisely. Location codes must be unique within a warehouse. Warehouses with active stock cannot be deactivated.

**Why this priority**: Warehouse and location data is the foundation for the Inventory module. Locations enable fine-grained stock tracking and picking.

**Independent Test**: Can be fully tested by creating a warehouse with a unique code, adding locations to it, and verifying that duplicate location codes within the same warehouse are rejected. Locations in different warehouses may share codes.

**Acceptance Scenarios**:

1. **Given** warehouse "WH-HN" does not exist, **When** the Admin creates a warehouse with code "WH-HN", **Then** the warehouse is created with status ACTIVE
2. **Given** warehouse "WH-HN" already has location "A01", **When** the Admin creates another location "A01" in the same warehouse, **Then** the system rejects with a duplicate-location-code error
3. **Given** warehouse "WH-HN" contains active stock, **When** the Admin attempts to deactivate it, **Then** the system rejects the deactivation
4. **Given** a location is INACTIVE, **When** a user attempts to stock-in to that location, **Then** the system rejects the operation

---

### User Story 3 - Admin manages production lines and machines (Priority: P2)

The factory floor is organized into production lines, each containing multiple machines. The Admin needs to define production lines and the machines within them so that production schedules can be assigned to specific lines and machines. Machines have operational states and must follow valid status transitions.

**Why this priority**: Production line and machine data is the foundation for the Production Execution and Maintenance modules. Without accurate machine definitions, work orders cannot be dispatched.

**Independent Test**: Can be fully tested by creating a production line, adding machines to it, and verifying machine status transitions respect business rules.

**Acceptance Scenarios**:

1. **Given** production line "LINE-01" does not exist, **When** the Admin creates it with required details, **Then** it is created successfully with status ACTIVE
2. **Given** machine "M-001" exists with status DOWN, **When** the system attempts to start a production run on it, **Then** the system rejects the operation
3. **Given** production line "LINE-01" is INACTIVE, **When** a work order is assigned to it, **Then** the system rejects the assignment

---

### User Story 4 - Admin controls machine status transitions (Priority: P3)

Machines on the factory floor change state throughout their lifecycle: available for work, currently running, down due to fault, under maintenance, or retired. The Admin needs to update machine status to reflect real-world conditions, and the system must enforce valid transitions to prevent operational errors.

**Why this priority**: Machine status directly affects production execution. However, the basic CRUD can work without the full state machine, so this is lower priority than creating the machines themselves.

**Independent Test**: Can be fully tested by changing a machine from AVAILABLE to UNDER_MAINTENANCE and back, and verifying that invalid transitions (e.g., RETIRED back to AVAILABLE) are rejected.

**Acceptance Scenarios**:

1. **Given** machine "M-001" is AVAILABLE, **When** the Admin transitions it to UNDER_MAINTENANCE, **Then** the transition succeeds
2. **Given** machine "M-001" is UNDER_MAINTENANCE, **When** the system attempts to start a production run, **Then** the operation is rejected
3. **Given** machine "M-001" is RETIRED, **When** the Admin attempts to start a production run, **Then** the operation is rejected
4. **Given** machine "M-001" is RUNNING, **When** a fault is reported, **Then** the status may transition to DOWN

---

### Edge Cases

- Duplicate product code on creation and update (unique constraint violation)
- Deactivating a product that has stock movement history is rejected
- Deactivating a warehouse or location that contains active stock is rejected
- Adding a location to a non-existent warehouse is rejected
- Machine status DOWN, UNDER_MAINTENANCE, or RETIRED prevents production start
- Attempting to assign a work order to an INACTIVE production line is rejected
- Concurrent updates to the same product must be handled via optimistic locking
- Location code uniqueness enforced only within the same warehouse (cross-warehouse duplicates allowed)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow Admin to create, read, update, and deactivate Products with fields: code, name, product type, unit, and status
- **FR-002**: System MUST enforce unique product codes across all products
- **FR-003**: System MUST prevent hard-deletion of products — only status-based deactivation (soft delete) is allowed
- **FR-004**: System MUST prevent deactivation of a product that has been referenced in any stock movement
- **FR-005**: System MUST allow Admin to create, read, update, and deactivate Warehouses with fields: code, name, address, and status
- **FR-006**: System MUST enforce unique warehouse codes
- **FR-007**: System MUST prevent deactivation of a warehouse that contains active stock
- **FR-008**: System MUST allow Admin to create, read, update, and deactivate Warehouse Locations with fields: warehouse, code, name, and status
- **FR-009**: System MUST enforce unique location codes within the same warehouse
- **FR-010**: System MUST prevent stock-in operations to INACTIVE locations
- **FR-011**: System MUST allow Admin to create, read, update, and deactivate Production Lines with fields: code, name, and status
- **FR-012**: System MUST prevent assigning work orders to INACTIVE production lines
- **FR-013**: System MUST allow Admin to create, read, update, and deactivate Machines with fields: production line, code, name, and status
- **FR-014**: System MUST enforce unique machine codes
- **FR-015**: System MUST support the following machine statuses: AVAILABLE, RUNNING, DOWN, UNDER_MAINTENANCE, RETIRED
- **FR-016**: System MUST prevent starting production on a machine whose status is DOWN, UNDER_MAINTENANCE, or RETIRED
- **FR-017**: System MUST support changing machine status and enforce valid transitions [NEEDS CLARIFICATION: the full set of valid machine status transitions is not defined — e.g., can RUNNING transition directly to UNDER_MAINTENANCE? Can RETIRED be reversed? A matrix of allowed transitions is needed.]
- **FR-018**: Product unit MUST be selected from a predefined set of units of measure (not free-text)
- **FR-019**: System MUST support listing products with optional filtering by product type and status
- **FR-020**: System MUST support listing warehouses, locations, production lines, and machines with pagination

### Non-functional Requirements

- Master Data APIs require role ADMIN
- UUID generated by application (UUID.randomUUID()), not by database
- Optimistic locking via version column on products
- Audit columns (created_at, created_by, updated_at, updated_by) on all tables
- All list endpoints must support pagination with default page size of 20

### Key Entities *(include if feature involves data)*

- **Product**: Core material or good entity. Has a unique code, name, product type (RAW_MATERIAL, SEMI_FINISHED, FINISHED_GOOD, CONSUMABLE, SPARE_PART), unit of measure, and lifecycle status. Referenced by inventory, BOM, and work order modules.
- **Warehouse**: Physical storage location. Has a unique code, name, address, and status. Contains zero or more locations.
- **Warehouse Location**: A specific bin/shelf/area within a warehouse. Location code is unique within the parent warehouse. Holds inventory quantities.
- **Production Line**: An assembly or manufacturing line on the factory floor. Has a code, name, and status. Contains one or more machines.
- **Machine**: An individual piece of equipment on a production line. Has a unique code, name, current operational status, and belongs to exactly one production line.
- **Product Type** (lookup): System-defined classification (RAW_MATERIAL, SEMI_FINISHED, FINISHED_GOOD, CONSUMABLE, SPARE_PART).
- **Unit of Measure** (lookup): System-defined units (piece, kg, liter, meter, etc.).
- **Machine Status** (lookup): System-defined operational states (AVAILABLE, RUNNING, DOWN, UNDER_MAINTENANCE, RETIRED).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Admin can create a new product with all required fields in under 3 steps and receive immediate confirmation
- **SC-002**: Duplicate-code violations are caught and reported to the Admin within 1 second of submission
- **SC-003**: All soft-delete business rules (product with stock, warehouse with stock, location with stock) are enforced with clear error messages
- **SC-004**: Machine status transitions respect the defined state machine — no invalid transition is accepted
- **SC-005**: List endpoints return results with pagination metadata and support at least one filter criterion
- **SC-006**: All master data operations complete successfully under normal load without data corruption

## Assumptions

- Only users with the ADMIN role can create, update, or deactivate master data. Read access may be granted to other roles.
- Product types (RAW_MATERIAL, SEMI_FINISHED, FINISHED_GOOD, CONSUMABLE, SPARE_PART) are system-defined seeds — [NEEDS CLARIFICATION: should Admin be able to add/modify product types, or are they fixed enums?]
- Units of measure are seeded as system reference data and managed separately.
- Machine statuses (AVAILABLE, RUNNING, DOWN, UNDER_MAINTENANCE, RETIRED) are system-defined.
- All entities use UUID identifiers generated by the application, not the database.
- All entities have standard audit columns: created timestamp, created by user, last updated timestamp, last updated by user.
- Optimistic locking (version column) is used for Products to prevent concurrent update conflicts.
- Pagination defaults to page size of 20 with standard page/offset parameters.
- The set of valid machine status transitions will be defined as a transition matrix during the planning phase.
