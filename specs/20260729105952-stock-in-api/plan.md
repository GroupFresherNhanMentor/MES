# Implementation Plan: Stock-In API & Stock Lot Querying

**Branch**: `20260729105952-stock-in-api` | **Date**: 2026-07-29 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/20260729105952-stock-in-api/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Implement physical stock receipt processing via `POST /api/stock-in` and enhanced paginated stock lot searching via `GET /api/stock-lots`. The implementation validates lot ownership against the requested product (reusing existing lots if matched, rejecting if assigned to another product), updates stock balances atomically, and logs `PURCHASE_IN` ledger movements.

## Technical Context

**Language/Version**: Java 25 / Spring Boot 4.1.0

**Primary Dependencies**: jOOQ 3.21, Spring Web, Spring Security, MapStruct, Lombok

**Storage**: PostgreSQL 18

**Testing**: JUnit 5, Mockito, AssertJ

**Target Platform**: Linux server

**Project Type**: Clean Architecture Web Application

**Performance Goals**: <2s API response latency for stock-in processing

**Constraints**: Zero data discrepancy between balance updates and ledger entries

**Scale/Scope**: 100k SKU stock management

## Constitution Check

*PASS* — Follows Clean Architecture layer boundaries:
- Presentation: `InventoryController.java`
- Application: `InventoryUseCase`, `InventoryService`, `StockInRequest`, `StockLotSearchRequest`
- Domain: `StockLotRepository`, `StockBalanceRepository`, `StockMovementRepository`, `MovementTypeConstants`, `StockStatusConstants`
- Infrastructure: `StockBalancePersistenceAdapter`, `StockLotPersistenceAdapter`, `StockMovementPersistenceAdapter`

## Project Structure

### Documentation (this feature)

```text
specs/20260729105952-stock-in-api/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# [REMOVE IF UNUSED] Option 2: Web application (when "frontend" + "backend" detected)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# [REMOVE IF UNUSED] Option 3: Mobile + API (when "iOS/Android" detected)
api/
└── [same as backend above]

ios/ or android/
└── [platform-specific structure: feature modules, UI flows, platform tests]
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
