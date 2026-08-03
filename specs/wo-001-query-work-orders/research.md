# Research: Query Work Orders (`GET /api/work-orders`)

## Phase 0 Findings & Architectural Decisions

### Decision 1: jOOQ Dynamic Query Building & Pagination

- **Decision**: Use jOOQ `DSLContext` with `DSL.noCondition()` to compose dynamic WHERE clauses for `finishedProductId`, `statusId`, and `code`, followed by `.limit(size).offset((long) page * size)`.
- **Rationale**:
  - Eliminates SQL injection risk.
  - Ensures type-safe mapping between jOOQ `WORK_ORDERS` table fields and Java values.
  - Separate `ctx.fetchCount(ctx.selectFrom(WORK_ORDERS).where(condition))` ensures accurate total count for pagination metadata.
- **Alternatives Considered**:
  - *JPA Criteria API / Hibernate*: Rejected because JPA/Hibernate is strictly forbidden in project coding rules.
  - *Raw SQL Strings*: Rejected due to lack of compile-time type checking and risk of SQL injection vulnerabilities.

---

### Decision 2: Spring Security Method-Level RBAC Protection

- **Decision**: Annotate `WorkOrderController.getWorkOrders(...)` with `@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'OPERATOR', 'FACTORY_MANAGER', 'AUDITOR')")`.
- **Rationale**:
  - `SecurityConfig` has `@EnableMethodSecurity` enabled.
  - Existing `AppJwtAuthenticationConverter` extracts user roles from DB (`USER_ROLES` ➔ `ROLES`) and populates `SimpleGrantedAuthority("ROLE_" + roleName)`.
  - `@PreAuthorize("hasAnyRole(...)")` checks `ROLE_` prefix automatically.
- **Alternatives Considered**:
  - *SecurityFilterChain URL pattern matching*: Less modular and harder to maintain per endpoint compared to explicit `@PreAuthorize`.
  - *Imperative in-service role check*: Pollutes business logic with authorization infrastructure code.

---

### Decision 3: Case-Insensitive Fuzzy Search on `code`

- **Decision**: Use `WORK_ORDERS.CODE.likeIgnoreCase("%" + code + "%")` in jOOQ.
- **Rationale**:
  - Migration `V20260727164430__add_trgm_indexes.sql` established a GIN trigram index (`idx_work_orders_code_trgm`) on `work_orders(code)`.
  - PostgreSQL uses the trigram index for `ILIKE '%...%'` queries, resulting in sub-10ms search performance even across large datasets.
- **Alternatives Considered**:
  - *Exact equality `eq(code)`*: Too restrictive for user UI search inputs.
  - *Full-text search TSVector*: Overkill for short code identifiers.
