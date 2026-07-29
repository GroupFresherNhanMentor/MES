# Feature Specification: BOM List Page (Frontend)

**Feature Branch**: `feature/bom-fe-001-bom-list`

**Created**: 2026-07-29

**Status**: Draft

**Input**: User description: "BOM List Page — bom-list"

## User Scenarios & Testing *(mandatory)*

### User Story 1 — View & Filter BOM List (Priority: P1)

As a Planner, Factory Manager, or Operator, I want to view a paginated list of Bills of Materials (BOMs) with status indicators, and filter by finished product and status so that I can easily find and inspect product structures.

**Why this priority**: Essential landing page for BOM management in the MES Angular frontend.

**Independent Test**: Navigate to `/boms` on the Angular UI, verify the table renders BOM entries with product name, version, item count, and status badge. Change filter dropdowns and verify filtered API results.

**Acceptance Scenarios**:

1. **Given** an authenticated user on `/boms`, **When** the page loads, **Then** the system fetches and displays a paginated list of BOMs matching `BomDto` structure (`finishedProductCode/Name`, `version`, `itemsCount`, `bomStatusName`, `createdBy`, `createdAt`).
2. **Given** filter dropdowns for `Finished Product` and `BOM Status`, **When** the user selects a product or status and clicks Filter, **Then** the list updates to display only matching BOM records via `GET /api/boms?finishedProductId=...&bomStatusId=...`.
3. **Given** the BOM list, **When** the user clicks on a BOM row, **Then** the UI navigates to the BOM Detail page at `/boms/:id`.
4. **Given** an authorized user (`ADMIN` or `PLANNER`), **When** viewing the BOM list page, **Then** a "Create BOM" button is visible in the page header. Clicking it opens the Create BOM Dialog.

---

### Edge Cases

- **Empty state**: If no BOMs match the current filter criteria, display a user-friendly empty state message ("No BOMs found").
- **Product filter options**: Finished product dropdown should list products of types `FINISHED_GOOD` and `SEMI_FINISHED`.
- **Status options**: Status filter dropdown should be populated dynamically from `GET /api/boms/statuses`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-FE-BOM-001**: Page MUST be accessible at route `/boms` using Angular Material Table and Tailwind CSS utility classes.
- **FR-FE-BOM-002**: Page MUST support filter inputs: `finishedProductId` (autocomplete/select) and `bomStatusId` (select).
- **FR-FE-BOM-003**: Page MUST support server-side pagination via `MatPaginator` (`page` and `size` parameters).
- **FR-FE-BOM-004**: Table MUST display columns: Product Code, Product Name, Version (`vX`), Items Count, Status Badge (`ff-badge` styling), Created By, Created At, and Actions.
- **FR-FE-BOM-005**: Header MUST include a "Create BOM" button for users with role `ADMIN` or `PLANNER`.

### Key Entities *(include if feature involves data)*

- **`BomDto`**: `{ id, finishedProductId, finishedProductName, finishedProductCode, version, bomStatusId, bomStatusName, createdBy, createdAt, items }`
- **`BomStatus`**: `{ id, name, description }` (`DRAFT`, `ACTIVE`, `INACTIVE`)
- **`ProductDto`**: Source for finished product filter options.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: BOM List page renders within < 200ms with smooth loading states.
- **SC-002**: Filter selections correctly update table data with matching API parameters.
- **SC-003**: Status badges use standard `.ff-badge` CSS classes (`ff-badge--active`, `ff-badge--pending`, `ff-badge--cancelled`).

## Assumptions

- Uses `ApiService` to communicate with backend or `mock.interceptor.ts`.
- Uses Angular Signals API for state management (`signal`, `computed`).
