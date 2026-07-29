# Feature Specification: Create BOM Dialog (Frontend)

**Feature Branch**: `feature/bom-fe-002-create-dialog`

**Created**: 2026-07-29

**Status**: Draft

**Input**: User description: "Create BOM Dialog — bom-create-dialog"

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Create BOM Header (Priority: P1)

As a Planner or Admin, I want to open a modal dialog to select a finished/semi-finished product and create a new BOM header in `DRAFT` status so that I can proceed to add component items in the BOM Detail page.

**Why this priority**: Core entry point for defining product structures and manufacturing recipes.

**Independent Test**: Click "Create BOM" button on `/boms`, verify `MatDialog` opens with a product selection dropdown (filtered to `FINISHED_GOOD` and `SEMI_FINISHED`) and a version input. Select a product, submit the form, and verify that HTTP `POST /api/boms` is called and the UI navigates to `/boms/:id`.

**Acceptance Scenarios**:

1. **Given** an authorized user (`ADMIN` or `PLANNER`) on `/boms`, **When** clicking "Create BOM", **Then** the `BomCreateDialog` modal opens cleanly with fields for Finished Product and Version.
2. **Given** the Finished Product dropdown in the dialog, **When** loading product options, **Then** only products with type `FINISHED_GOOD` or `SEMI_FINISHED` are displayed as selectable options.
3. **Given** valid form selections (`finishedProductId` selected, `version >= 1`), **When** clicking "Create BOM", **Then** the dialog submits `POST /api/boms`, closes the modal upon HTTP 201/200 success, and navigates the user to `/boms/:newBomId`.
4. **Given** a user attempting to submit without selecting a product, **When** clicking "Create BOM", **Then** validation error is displayed and submission is blocked.

---

### Edge Cases

- **Product already has an active BOM**: The backend will allow creating a new version in `DRAFT` status; the dialog does not block creation as multiple versions can exist before activation.
- **Dialog Cancellation**: Clicking "Cancel" or pressing `Esc` closes the dialog without mutating state or making API calls.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-FE-BOM-006**: Component MUST be implemented as a standalone Angular Material Dialog component (`BomCreateDialog`).
- **FR-FE-BOM-007**: Dialog MUST use `ReactiveFormsModule` / `FormBuilder` with validation for `finishedProductId` (`Validators.required`) and `version` (`Validators.min(1)`).
- **FR-FE-BOM-008**: Product selection dropdown MUST fetch products filtered by types `FINISHED_GOOD` and `SEMI_FINISHED` (`GET /api/products?productTypeId=PT-FIN,PT-SUB`).
- **FR-FE-BOM-009**: On successful creation via `POST /api/boms`, dialog MUST close and trigger navigation to `/boms/:id`.

### Key Entities *(include if feature involves data)*

- **`CreateBomRequest`**: `{ finishedProductId: string, version?: number, bomStatusId?: string }`
- **`BomDto`**: Response object returned from `POST /api/boms`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Modal opens within < 100ms with smooth Angular Material animation.
- **SC-002**: Validation prevents submitting invalid or incomplete form data.
- **SC-003**: Navigation to `/boms/:id` occurs immediately upon API success.

## Assumptions

- Uses `MatDialog` (`dialog.open(BomCreateDialog)`).
- Tailwind CSS utility classes used for dialog internal layout (no `.scss` file).
