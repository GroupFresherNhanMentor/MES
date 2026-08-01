# Research & Design Decisions: Audit Log for Important Actions (FR-AUD-001)

## Technical Research & Findings

### 1. Audit Log Recording Pattern
- **Decision**: Provide an `AuditLogPort` / `AuditLogService` that can be invoked across application services when critical operations occur (e.g. `CREATE_WORK_ORDER`, `QC_PASS`, `ACTIVATE_BOM`, `ADJUST_STOCK`).
- **Rationale**: Direct application port invocation within service transactions guarantees audit log persistence is tightly bound to business state changes and rolls back automatically if the business transaction fails.
- **Alternatives Considered**:
  - *Spring AOP / Aspect*: Rejected as primary mechanism for complex entity state changes because `oldValue` and `newValue` diff calculation requires explicit entity state domain inspection before and after modification.
  - *PostgreSQL CDC / Debezium*: Rejected for v1 due to operational overhead; inline transactional persistence ensures instant zero-lag compliance auditing.

### 2. Immutability & Protection Against Modifications
- **Decision**: Implement dual-layer protection against updates and deletes:
  1. Persistence layer (`AuditLogPersistenceAdapter`): Throws `UnsupportedOperationException` if `update` or `delete` is called.
  2. Database layer (`Flyway migration`): Define a PostgreSQL trigger / rule on `audit_logs` table that raises an exception on `BEFORE UPDATE OR DELETE`.
- **Rationale**: Strictly meets Business Rule "Audit log không được sửa hoặc xóa" and guarantees tamper-proof log storage even against direct SQL script execution.

### 3. Data Representation of `oldValue` and `newValue`
- **Decision**: Store `oldValue` and `newValue` as `JSONB` columns in PostgreSQL, exposed as structured `String` / `JsonNode` in Java DTOs.
- **Rationale**: JSONB allows flexible state capture across diverse domain entities (Work Orders, Stock Balances, Maintenance Tickets, BOMs) without rigid entity-specific audit table schemas.

### 4. Integration with `CurrentUserPort`
- **Decision**: `AuditLogService` automatically fetches `actorId` from `CurrentUserPort.getCurrentUserId()` if `actorId` is not explicitly passed.
- **Rationale**: Aligns with Clean Architecture governance rules (`docs/rules/backend-architecture.md`) established in the MES system.
