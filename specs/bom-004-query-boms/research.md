# Research & Technical Decisions: Get BOM List and BOM Detail (Query BOMs)

**Feature**: Query BOMs (`bom-004-query-boms`)  
**Created**: 2026-07-28  

## 1. jOOQ Dynamic Condition Building for Filters

### Problem Statement
The `GET /api/boms` endpoint supports optional filter parameters: `finishedProductId` and `bomStatusId`.

### Decision
`BomPersistenceAdapter` will dynamically build jOOQ `Condition` objects:
```java
Condition condition = DSL.trueCondition();
if (finishedProductId != null) {
    condition = condition.and(BOMS.FINISHED_PRODUCT_ID.eq(finishedProductId));
}
if (bomStatusId != null) {
    condition = condition.and(BOMS.BOM_STATUS_ID.eq(bomStatusId));
}

List<Bom> items = dslCtx.selectFrom(BOMS)
        .where(condition)
        .orderBy(BOMS.CREATED_AT.desc())
        .limit(size)
        .offset(offset)
        .fetch(r -> mapper.toDomain(r));

long total = dslCtx.fetchCount(BOMS, condition);
```

### Rationale
Ensures clean, type-safe dynamic SQL building without SQL injection risks or redundant queries.

---

## 2. N+1 Prevention & Complete Item Retrieval in Get BOM Detail

### Problem Statement
When calling `GET /api/boms/{id}`, the system must return complete header metadata along with all linked `bom_items`.

### Decision
`BomPersistenceAdapter.findById(UUID id)` already executes a 2-step single-key query:
1. Select record from `boms` where `id = :id`.
2. Select records from `bom_items` where `bom_id = :id`.
3. Assemble domain entity `Bom` with `items` list and convert via MapStruct mapper `toDto(bom)`.

### Rationale
Prevents N+1 database roundtrips and guarantees that `GET /api/boms/{id}` always yields complete component specifications.
