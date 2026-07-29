# Tasks: Create BOM Dialog (Frontend)

**Input**: Design documents from `specs/bom-fe-002-create-dialog/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable
- **[US1]**: User Story 1 - Create BOM Header (Priority: P1)

---

## Phase 1: Setup & Target Directory

**Purpose**: Create component folder structure

- [x] T001 Create component directory `fe/src/app/features/boms/components/bom-create-dialog/`

---

## Phase 2: User Story 1 — Create BOM Header (Priority: P1) 🎯 MVP

**Goal**: Implement `BomCreateDialog` standalone component with Reactive Form validation, product fetching, API submit, and integration into `BomList`.

**Independent Test**: Open `/boms`, click "Create BOM", select product, submit form, verify dialog closes and UI navigates to `/boms/:id`.

### Implementation Tasks for User Story 1

- [x] T002 [US1] Create `BomCreateDialog` standalone component in `fe/src/app/features/boms/components/bom-create-dialog/bom-create-dialog.ts` with Reactive Form, product options fetching, and `POST /api/boms` submission
- [x] T003 [US1] Integrate `MatDialog` into `fe/src/app/features/boms/pages/bom-list/bom-list.ts` to trigger `BomCreateDialog` on `onCreateBom()` and navigate to `/boms/:id` on success

---

## Phase 3: Polish & Verification

- [x] T004 Confirm clean TypeScript compilation (`npx tsc --noEmit`) and verify modal dialog styling and form validation interaction

---

## Phase 4: Convergence

- [x] T005 Add error feedback to `BomCreateDialog` — display server error message in dialog when `POST /api/boms` fails (missing: US1/AC3)
- [x] T006 Add component test for `BomCreateDialog` — verify render, form validation, product loading, submit success, submit failure, and dialog cancel per Constitution VI (contradicts: Constitution VI)

## Dependencies & Execution Order

- **Phase 1 (Setup)**: Blocks Phase 2.
- **Phase 2 (US1)**: Implement T002 first, then T003.
- **Phase 3 (Polish)**: Verify after Phase 2 completion.
- **Phase 4 (Convergence)**: T005 first (error handling), then T006 (tests).
