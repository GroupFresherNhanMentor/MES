# Implementation Plan: BOM List Page (Frontend)

**Branch**: `feature/bom-fe-001-bom-list` | **Date**: 2026-07-29 | **Spec**: [spec.md](file:///e:/Fresher/ojt/MES/specs/bom-fe-001-bom-list/spec.md)

**Input**: Feature specification from `/specs/bom-fe-001-bom-list/spec.md`

## Summary

This feature enhances the BOM List Page (`bom-list`) in the Angular MES frontend (`fe/src/app/features/boms/pages/bom-list/bom-list.ts`). It implements filtering by finished product and BOM status, server-side pagination, status badge styling using Tailwind utility classes (`.ff-badge`), row click navigation to BOM detail (`/boms/:id`), and role-based action visibility (displaying the "Create BOM" button for `ADMIN` and `PLANNER` roles via `AuthService`).

## Technical Context

**Language/Version**: TypeScript 5.7 / Angular 22 / Tailwind CSS v4 / Angular Material 22  
**Primary Dependencies**: `@angular/core` (Signals API: `signal`, `computed`), `@angular/material` (`MatTableModule`, `MatPaginatorModule`, `MatSelectModule`, `MatButtonModule`, `MatIconModule`, `MatCardModule`)  
**State Management**: Angular Signals (`items`, `total`, `page`, `size`, `selectedProductId`, `selectedStatusId`)  
**Styling**: Tailwind CSS utility classes in inline template (Strictly NO component `.scss` files per `frontend-tailwind.md` rule)  
**Security/Role**: Role check via `AuthService.getCurrentUser()?.role` (`ADMIN` or `PLANNER` to show Create BOM button)  
**Constraints**: Follow `docs/rules/frontend-tailwind.md` styling rules  

## Constitution & Frontend Rules Check

- No component `.scss` file used (Tailwind utility classes in template only)
- No `style=""` inline attributes
- Status badges use standard `.ff-badge` and `.ff-badge--*` classes
- Role checking uses `AuthService.getCurrentUser()`

## Project Structure

### Documentation (this feature)

```text
specs/bom-fe-001-bom-list/
├── plan.md              # This file
├── research.md          # Research findings
├── data-model.md        # Model & state specifications
├── quickstart.md        # UI validation scenarios guide
└── contracts/
    └── bom-list-api.json  # OpenAPI contract specification for BOM list & filter endpoints
```

### Target Component File

```text
fe/src/app/features/boms/pages/bom-list/
└── bom-list.ts          # Angular standalone component (modified)
```

**Structure Decision**: Refactor existing standalone component `bom-list.ts` using Angular Signals, Reactive Forms/MatSelect, and Tailwind CSS.
