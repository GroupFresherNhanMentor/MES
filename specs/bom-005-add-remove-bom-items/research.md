# Phase 0 Research Findings: Add and Remove BOM Items

## 1. DRAFT Immutability Guard Pattern

- **Decision**: Validate target BOM status in `BomService.addBomItem` and `BomService.deleteBomItem` before modifying items.
- **Rationale**: Non-DRAFT BOMs (`ACTIVE`, `INACTIVE`) are frozen to prevent breaking active production Work Orders. Modifications require creating a new DRAFT version first via `POST /api/boms/{id}/new-version`.
- **Alternatives Considered**: Soft-deleting items or allowing modifications with version bumps. Rejected because explicit versioning guarantees full auditability.

## 2. Validation & Error Codes

- **Decision**:
  - If BOM does not exist: Throw `BomNotFoundException` (HTTP 404).
  - If BOM is not in `DRAFT` status: Throw `InvalidBomStatusException` (HTTP 400).
  - If `quantityPerUnit` <= 0 or invalid input: Bean Validation `@Positive` throws `MethodArgumentNotValidException` (HTTP 400).
- **Rationale**: Aligns with RFC 9457 / Spring `AppException` error handling standards in `common.exception`.

## 3. Security Role Constraints

- **Decision**: Restrict endpoints `@PostMapping("/{bomId}/items")` and `@DeleteMapping("/{bomId}/items/{itemId}")` to `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")`.
- **Rationale**: Operators and QC Inspectors can query BOM details but cannot edit production material structures.
