# Implementation Plan: Create BOM Dialog (Frontend)

**Branch**: `feature/bom-fe-002-create-dialog` | **Date**: 2026-07-29 | **Spec**: [spec.md](file:///e:/Fresher/ojt/MES/specs/bom-fe-002-create-dialog/spec.md)

**Input**: Feature specification from `/specs/bom-fe-002-create-dialog/spec.md`

## Summary

This feature creates the `BomCreateDialog` component (`fe/src/app/features/boms/components/bom-create-dialog/bom-create-dialog.ts`), allowing Planners and Admins to define a new BOM header. It loads products filtered to types `FINISHED_GOOD` and `SEMI_FINISHED`, validates user inputs via Reactive Forms, posts the payload to `POST /api/boms`, and automatically navigates the UI to `/boms/:id` upon creation. It also integrates `MatDialog` into `BomList`.

## Technical Context

**Language/Version**: TypeScript 5.7 / Angular 22 / Tailwind CSS v4 / Angular Material 22  
**Primary Dependencies**: `@angular/core` (Signals API: `signal`), `@angular/forms` (`ReactiveFormsModule`, `FormBuilder`, `Validators`), `@angular/material/dialog` (`MatDialog`, `MatDialogRef`, `MatDialogModule`)  
**State Management**: Reactive Forms + Angular Signals  
**Styling**: Tailwind CSS utility classes in template (NO component `.scss` files)  
**Security/Role**: Openable from `BomList` only when `canCreate()` is true (`ADMIN` or `PLANNER`)  

## Constitution & Frontend Rules Check

- No component `.scss` file used (Tailwind utility classes in template only)
- No `style=""` inline attributes
- Form validation error messages styled using design tokens
- Material dialog overrides use `!` prefix where applicable

## Project Structure

### Documentation (this feature)

```text
specs/bom-fe-002-create-dialog/
├── plan.md              # This file
├── research.md          # Research findings
├── data-model.md        # Form model & state specifications
├── quickstart.md        # UI validation scenarios guide
└── contracts/
    └── bom-create-api.json  # OpenAPI contract specification for Create BOM endpoint
```

### Component Files

```text
fe/src/app/features/boms/components/bom-create-dialog/
└── bom-create-dialog.ts # Standalone Angular Material Dialog component
```

**Structure Decision**: Create `bom-create-dialog.ts` as a standalone component inside `fe/src/app/features/boms/components/bom-create-dialog/`.
