# Backend Query Optimization Rules

## N+1 Prevention (NON-NEGOTIABLE)

Never call a repository method inside a loop or stream. Every loop-per-row pattern is an N+1.

```java
// FORBIDDEN — N+1: 1 query for boms + N queries for items
List<Bom> boms = bomRepository.findAll();
boms.forEach(bom -> {
    List<BomItem> items = bomItemRepository.findByBomId(bom.getId()); // N queries
});

// REQUIRED — 1 query total (see techniques below)
```

---

## Technique 1: JOIN + fetchGroups() for 1:N relationships

Use a single JOIN and group results in memory. Best for moderate data sizes.

```java
// Fetch Boms with their Items in one query
Map<BomsRecord, List<BomItemsRecord>> grouped = dslCtx
    .select()
    .from(BOMS)
    .leftJoin(BOM_ITEMS).on(BOM_ITEMS.BOM_ID.eq(BOMS.ID))
    .fetchGroups(BOMS, BOM_ITEMS);

List<Bom> boms = grouped.entrySet().stream()
    .map(e -> mapper.toDomain(e.getKey(), e.getValue()))
    .collect(java.util.stream.Collectors.toList());
```

---

## Technique 2: multiset() for nested collections (preferred for complex graphs)

jOOQ 3.15+ `multiset()` fetches a nested collection inside a single SQL query using a correlated
subquery. Cleaner than JOIN + grouping when there are multiple nested collections.

```java
// Fetch Boms with nested Items list — single round-trip
List<Bom> boms = dslCtx
    .select(
        BOMS.ID,
        BOMS.FINISHED_PRODUCT_ID,
        BOMS.VERSION,
        multiset(
            select(BOM_ITEMS.fields())
            .from(BOM_ITEMS)
            .where(BOM_ITEMS.BOM_ID.eq(BOMS.ID))
        ).as("items").convertFrom(r -> r.map(mapper::itemToDomain))
    )
    .from(BOMS)
    .fetch(r -> Bom.builder()
        .id(r.get(BOMS.ID))
        .finishedProductId(r.get(BOMS.FINISHED_PRODUCT_ID))
        .version(r.get(BOMS.VERSION))
        .items(r.get("items", List.class))
        .build());
```

Use `multiset()` when an entity has **multiple independent nested collections** — avoids the
cartesian product that JOIN produces in those cases.

---

## Technique 3: Batch IN query for loading related entities by IDs

When you already have a list of IDs and need to load related data, use `IN` — never loop.

```java
// FORBIDDEN
List<UUID> productIds = boms.stream().map(b -> b.getFinishedProductId()).toList();
productIds.forEach(id -> productRepository.findById(id)); // N queries

// REQUIRED — 1 query
List<UUID> productIds = boms.stream().map(b -> b.getFinishedProductId()).toList();
Map<UUID, Product> productsById = dslCtx
    .selectFrom(PRODUCTS)
    .where(PRODUCTS.ID.in(productIds))
    .fetchMap(PRODUCTS.ID, r -> productMapper.toDomain(r));

// Then join in memory
boms.forEach(bom -> {
    Product product = productsById.get(bom.getFinishedProductId());
});
```

---

## Technique 4: fetchMap() for 1:1 lookups

When joining produces a 1:1 relationship, use `fetchMap()` to get a typed map directly.

```java
// Load all machines indexed by ID in one query
Map<UUID, Machine> machinesById = dslCtx
    .selectFrom(MACHINES)
    .fetchMap(MACHINES.ID, r -> machineMapper.toDomain(r));
```

---

## Technique 5: JOIN for filtering/sorting by related fields

Never load a collection just to filter it in Java — push the predicate to SQL.

```java
// FORBIDDEN — loads everything then filters in Java
List<WorkOrder> all = workOrderRepository.findAll();
List<WorkOrder> filtered = all.stream()
    .filter(wo -> wo.getLineId().equals(lineId))
    .toList();

// REQUIRED — filter in SQL
List<WorkOrder> filtered = dslCtx
    .selectFrom(WORK_ORDERS)
    .where(WORK_ORDERS.LINE_ID.eq(lineId))
    .fetch(mapper::toDomain);
```

---

## General Rules

| Rule | Detail |
|------|--------|
| No repository call in a loop | Always batch with `IN` or use JOIN |
| No `.findAll()` then filter in Java | Add a `WHERE` clause |
| No `.findAll()` then sort in Java | Add `ORDER BY` to the query |
| Prefer `multiset()` for multiple nested collections | Avoids cartesian product from multi-JOIN |
| Use `fetchGroups()` after JOIN for single nested collection | Simpler than multiset for 1 level deep |
| Paginate list queries by default | Use `.limit(size).offset(page * size)` for operational datasets and every new list endpoint |
| Bound approved legacy catalogs | An unpaginated legacy catalog is allowed only under the compatibility conditions in `backend-api.md`; query it with deterministic `ORDER BY` and protect its response shape with a compatibility test |
| Count separately, not with fetchAll().size() | Use `dslCtx.fetchCount(table, condition)` |
