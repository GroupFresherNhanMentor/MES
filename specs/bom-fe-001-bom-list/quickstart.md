# Quickstart Validation Guide: BOM List Page

This guide describes how to validate the **BOM List Page** on the Angular UI.

## 1. Prerequisites

- Angular dev server running: `cd fe && npm run dev` (or `ng serve`)
- Navigate browser to `http://localhost:4200/boms`

## 2. Validation Scenarios

### Scenario 1: Initial Page Load (Happy Path)

- Open `http://localhost:4200/boms`
- **Verify**:
  - Page heading "Bill of Materials" displays with subtitle.
  - Filter bar displays dropdowns for "Finished Product" and "BOM Status".
  - BOM table renders columns: Product Code, Product Name, Version, Items Count, Status, Created By, Created At.
  - Status badges use appropriate status chip colors (`ACTIVE` green, `DRAFT` yellow, `INACTIVE` grey/dark).

### Scenario 2: Filter by Product & Status

- Select "Finished Widget A" in the Product filter.
- Click "Filter".
- **Verify**: Only BOMs matching "Finished Widget A" are displayed in the table.
- Click "Reset Filters".
- **Verify**: Full BOM list re-loads.

### Scenario 3: Role-based Action Visibility

- Log in as `PLANNER` or `ADMIN`:
  - **Verify**: "Create BOM" button is visible in top-right of page header.
- Log in as `OPERATOR` or `QC_INSPECTOR`:
  - **Verify**: "Create BOM" button is **hidden**.
