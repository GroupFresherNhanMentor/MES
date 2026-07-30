# Backend Tech Stack Rules

## Stack

| Concern | Required | Version |
|---------|----------|---------|
| Language | Java | 25 |
| Framework | Spring Boot | 4.1.0 |
| Database | PostgreSQL | 18 |
| Query builder | jOOQ | 3.21 |
| DB migrations | Flyway | latest |
| Object mapping | MapStruct | 1.6.3 |
| Boilerplate | Lombok | latest |
| Security | Spring Security + JWT (OAuth2 Resource Server) | — |
| API docs | Springdoc OpenAPI (Swagger UI) | — |
| Testing | JUnit 5 + Testcontainers | — |

## Rules Per Technology

### jOOQ
- All DB queries use `DSLContext` — no raw SQL strings, no JPQL, no HQL
- Import generated tables via `fpt.qn.mes.jooq.Tables.*`
- Use `fetchOptional`, `fetch`, `fetchOne` — not `fetchAny` unless intentional
- Pagination: use `.limit(size).offset(page * size)` + separate `fetchCount`
- Upsert pattern: `insertInto(...).set(record).onConflict(ID).doUpdate().set(record)`

### Flyway
- All schema changes go through Flyway migration scripts
- File naming: `V{version}__{description}.sql` (e.g., `V1__init_schema.sql`)
- Never modify an already-applied migration — create a new one
- Migration scripts live in `be/src/main/resources/db/migration/`

### MapStruct
- One `*DtoMapper` per domain entity in `application/mapper/`
- One `*RecordMapper` per jOOQ record in `infrastructure/persistence/`
- Always use `componentModel = "spring"`
- Never write manual mapping code — if MapStruct can't map it, add a `@Mapping` annotation

### Lombok
- `@Getter` on domain entities (not `@Data` — avoids accidental equals/hashCode on mutable state)
- `@Builder` for factory-style creation
- `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` on services, adapters, controllers
- `@RequiredArgsConstructor` for constructor injection (works with `makeFinal`)
- Never use `@Setter` on domain entities

### Spring Security / JWT
- Auth uses OAuth2 Resource Server with JWT
- Extract current user from `@AuthenticationPrincipal Jwt jwt` in controllers
- User ID: `UUID.fromString(jwt.getSubject())`
- Never access `SecurityContextHolder` directly in service layer — pass userId as parameter

### Validation
- Use Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Valid`) on request DTOs
- Validate at controller boundary only (`@Valid @RequestBody`)
- Never validate inside service or domain layers — trust the input contract

### Reference Data Seeding

All lookup/reference table data (statuses, types, priorities) must be seeded via JSON files in  
`be/src/main/resources/seed/`. Never hardcode seed values in Flyway migration scripts.

**Tables that require seed files:**

| Seed file | Table |
|-----------|-------|
| `roles.json` | `roles` |
| `product-types.json` | `product_types` |
| `product-statuses.json` | `product_statuses` |
| `units-of-measure.json` | `units_of_measure` |
| `warehouse-statuses.json` | `warehouse_statuses` |
| `location-statuses.json` | `location_statuses` |
| `line-statuses.json` | `line_statuses` |
| `machine-statuses.json` | `machine_statuses` |
| `lot-types.json` | `lot_types` |
| `stock-statuses.json` | `stock_statuses` |
| `movement-types.json` | `movement_types` |
| `bom-statuses.json` | `bom_statuses` |
| `work-order-statuses.json` | `work_order_statuses` |
| `work-order-priorities.json` | `work_order_priorities` |
| `work-order-event-types.json` | `work_order_event_types` |
| `qc-statuses.json` | `qc_statuses` |
| `qc-actions.json` | `qc_actions` |
| `defect-types.json` | `defect_types` |
| `maintenance-ticket-types.json` | `maintenance_ticket_types` |
| `maintenance-ticket-statuses.json` | `maintenance_ticket_statuses` |
| `maintenance-ticket-priorities.json` | `maintenance_ticket_priorities` |

**JSON format** (follow the existing `roles.json` pattern):
```json
[
  { "name": "ACTIVE",   "description": "..." },
  { "name": "INACTIVE", "description": "..." }
]
```

For tables with a `code` column, include it:
```json
[
  { "code": "RAW_MATERIAL", "name": "Raw Material", "description": "..." }
]
```

**Rules:**
- One file per table, named in `kebab-case` matching the table name
- Seed is loaded at application startup by a `DataSeeder` component using `INSERT ... ON CONFLICT DO NOTHING` so re-runs are safe
- Adding new entries: add to the JSON file — never patch the DB manually
- Never delete entries from a seed file if they are already referenced by existing data

## What Is Forbidden

| Forbidden | Use Instead |
|-----------|-------------|
| `JpaRepository`, `CrudRepository` | `domain.repository.*Repository` + jOOQ adapter |
| `EntityManager`, JPQL, HQL | jOOQ DSLContext |
| Manual field-by-field mapping | MapStruct `*DtoMapper` or `*RecordMapper` |
| `new RuntimeException(...)` | `new AppException(HttpStatus, errorCode, message)` |
| `@Autowired` field injection | Constructor injection via `@RequiredArgsConstructor` |
| Raw `List<Object[]>` from queries | Typed jOOQ record mappers |
| Importing infrastructure in application | Always depend on interfaces |
| `UUID.randomUUID()` for entity IDs | `UuidV7.generate()` from `fpt.qn.mes.common.util.UuidV7` |
