# Tasks: BOM List Page (Frontend)

**Input**: Design documents from `specs/bom-fe-001-bom-list/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `quickstart.md`

## Format: `- [ ] [ID] [P?] [Story?] Description with file path`

- **[P]**: Parallelizable
- **[US1]**: User Story 1 - View & Filter BOM List (Priority: P1)

---

## Phase 1: Setup & Dependencies

**Purpose**: Ensure component imports Angular Material form modules and AuthService

- [x] T001 Import `MatSelectModule`, `MatFormFieldModule`, `MatOptionModule`, and `AuthService` into `fe/src/app/features/boms/pages/bom-list/bom-list.ts`

---

## Phase 2: User Story 1 — View & Filter BOM List (Priority: P1) 🎯 MVP

**Goal**: Implement filter dropdowns, Angular Signals state, role-based Create BOM button, and row click navigation.

**Independent Test**: Open `/boms` on frontend, select filters, click row to navigate to `/boms/:id`.

### Implementation Tasks for User Story 1

- [x] T002 [US1] Add filter signals (`selectedProductId`, `selectedStatusId`, `productsList`, `statusesList`) and `canCreate` computed signal in `fe/src/app/features/boms/pages/bom-list/bom-list.ts`
- [x] T003 [US1] Implement filter bar UI (Finished Product select, BOM Status select, Reset filter button) and Create BOM header button in template of `fe/src/app/features/boms/pages/bom-list/bom-list.ts`
- [x] T004 [US1] Implement dropdown options loading (`GET /api/products`, `GET /api/boms/statuses`) and query filter params mapping in `load()` method of `fe/src/app/features/boms/pages/bom-list/bom-list.ts`
- [x] T005 [US1] Add row click navigation (`router.navigate(['/boms', row.id])`) and empty state display in `fe/src/app/features/boms/pages/bom-list/bom-list.ts`

---

## Phase 3: Polish & Verification

- [x] T006 Verify Tailwind CSS classes compliance (no component `.scss`, status badge `.ff-badge` styles) and test filter interactions on `/boms`

---

## Phase 4: Convergence

- [x] T007 [US1] Filter product dropdown to show only `FINISHED_GOOD` / `SEMI_FINISHED` types per spec edge case — add type filter param to `API.products.base` call in `loadDropdowns()` (partial: spec edge case)
- [x] T008 [US1] Add `loading` signal and `MatProgressBar` / spinner to template for smooth loading states per SC-001 (partial: SC-001)
- [x] T009 [US1] Add Actions column to `displayedColumns` per FR-FE-BOM-004 — placeholder for future detail/edit actions (partial: FR-FE-BOM-004)
- [x] T010 Add component test for BOM list page — at minimum verify render + filter + row click per Constitution VI (contradicts: Constitution VI)

## Dependencies & Execution Order

- **Phase 1 (Setup)**: Blocks Phase 2.
- **Phase 2 (US1)**: Sequentially implement T002 -> T003 -> T004 -> T005.
- **Phase 3 (Polish)**: Verify after Phase 2 completion.
- **Phase 4 (Convergence)**: Sequentially T007 → T008 → T009 → T010. T007, T008, T009 independent, T010 runs last.
