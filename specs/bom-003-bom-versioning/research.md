# Research & Technical Decisions: Bill of Materials Versioning (BOM Versioning)

**Feature**: BOM Versioning (`bom-003-bom-versioning`)  
**Created**: 2026-07-28  

## 1. Auto-incrementing Version Calculation

### Problem Statement
When cloning a new version from a source BOM, the system must assign `version = maxVersion + 1` for the target product to guarantee uniqueness and sequential ordering.

### Decision
`BomRepository` will implement `findMaxVersionByFinishedProductId(UUID finishedProductId)`:
```sql
SELECT COALESCE(MAX(version), 0) FROM boms WHERE finished_product_id = :productId;
```
The new version is calculated as `maxVersion + 1`.

### Rationale
Calculates the next version sequence reliably at transaction time regardless of which version was selected as the clone source.

---

## 2. Deep-Copying Component Line Items (`bom_items`)

### Problem Statement
Cloning a BOM must copy all existing `bom_items` to the new draft BOM header.

### Decision
When `createNewVersion(UUID sourceBomId)` is called:
1. Fetch source `Bom` entity along with its `items`.
2. Generate new `Bom` header entity with `newBomId = UUID.randomUUID()`, `version = maxVersion + 1`, `status = DRAFT`, and `createdBy = currentUserId`.
3. Save new `Bom` header entity to `boms`.
4. For each `BomItem` in the source BOM, create a new `BomItem` entity with `id = UUID.randomUUID()`, `bomId = newBomId`, and copied `materialProductId`, `quantityPerUnit`, `scrapRate`.
5. Bulk insert/save the new `BomItem` entities.

### Rationale
Preserves exact formula specification from the clone source while assigning brand new UUID primary keys to the cloned items in the new draft version.

---

## 3. Immutability Enforcement for NON-DRAFT BOMs

### Problem Statement
SRS FR-BOM-003 states that BOMs used in production/work orders (`ACTIVE` or `INACTIVE`) cannot be edited directly.

### Decision
Add status validation checks to all item modification service methods (`addBomItem` and `deleteBomItem`):
```java
if (!draftStatusId.equals(targetBom.getBomStatusId())) {
    throw new InvalidBomStatusException("Cannot modify items on non-DRAFT BOMs; create a new version instead");
}
```

### Rationale
Protects active production standards and historical records from accidental modification, forcing recipe changes to follow proper versioning workflow.
