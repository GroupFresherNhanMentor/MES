# Quickstart Validation Guide: Create BOM Dialog

This guide describes how to validate the **Create BOM Dialog** modal.

## 1. Prerequisites

- Angular dev server running: `cd fe && npm run dev`
- Logged in as `ADMIN` or `PLANNER`

## 2. Validation Scenarios

### Scenario 1: Open Dialog & Form Display

- Open `http://localhost:4200/boms`
- Click "Create BOM" button
- **Verify**:
  - Modal title "Create Bill of Materials" displays with a subtitle.
  - "Finished Product" select field is required.
  - "Version" field defaults to `1`.
  - Buttons "Cancel" and "Create BOM" render at the dialog footer.

### Scenario 2: Validation & Submission

- Leave "Finished Product" unselected and click "Create BOM".
- **Verify**: Form validation error appears ("Finished Product is required").
- Select "Finished Widget A (WIDGET-A)".
- Click "Create BOM".
- **Verify**:
  - `POST /api/boms` payload `{ finishedProductId: "P006", version: 1 }` is submitted.
  - Modal closes.
  - Page navigates to `/boms/:newBomId`.
