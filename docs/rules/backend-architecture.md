# Backend Architecture Rules

## Layer Structure

Every domain module follows this exact 4-layer structure. No exceptions.

```
{module}/
├── domain/
│   ├── entities/           # Pure Java domain objects — NO framework imports
│   └── repository/         # Repository interfaces (output ports)
├── application/
│   ├── port/
│   │   ├── in/             # Use case interfaces (input ports)
│   │   └── out/            # External dependency interfaces (output ports)
│   ├── service/            # Implements use case interfaces
│   ├── dto/
│   │   └── {feature}/      # One sub-package per feature/entity (e.g. inspection, qcstatus)
│   │       ├── {operation}/ # One sub-package per operation (create, search, pass, fail, …)
│   │       │   ├── {Feature}{Operation}Request.java
│   │       │   └── {Feature}{Operation}Response.java  # if operation-specific response needed
│   │       └── {Feature}Response.java  # shared response DTO reused across operations
│   ├── mapper/             # MapStruct domain ↔ DTO mappers
│   └── exception/          # Module-scoped exceptions (extend AppException)
├── infrastructure/
│   ├── persistence/        # jOOQ adapters + record mappers
│   └── {concern}/          # Other adapters (security, messaging, etc.)
└── presentation/           # Spring MVC REST controllers
```

### DTO sub-package rules

- **One sub-package per feature** inside `dto/` — never a flat `request/` or `response/` folder
- **One sub-package per operation** inside the feature package (`create/`, `search/`, `pass/`, `fail/`, …)
- **Request DTOs** live inside the operation sub-package: `dto/{feature}/{operation}/{Feature}{Operation}Request.java`
- **Response DTOs** that are operation-specific live alongside the request: `dto/{feature}/{operation}/{Feature}{Operation}Response.java`
- **Shared response DTOs** (returned by multiple operations or by GET endpoints) live directly in the feature package: `dto/{feature}/{Feature}Response.java`
- Never share a request DTO across features (e.g. no `CreateLookupRequest` used by both QcStatus and QcAction) — each feature owns its own request class

Example (quality module):
```
dto/
├── inspection/
│   ├── QualityInspectionResponse.java       # shared — used by GET list, GET by id, POST
│   ├── QualityInspectionResultResponse.java # shared nested DTO
│   ├── create/
│   │   └── CreateQualityInspectionRequest.java
│   ├── pass/
│   │   ├── PassQcRequest.java
│   │   └── PassQcResponse.java
│   └── fail/
│       ├── FailQcRequest.java
│       └── FailQcResponse.java
├── qcstatus/
│   ├── QcStatusResponse.java
│   ├── create/
│   │   └── CreateQcStatusRequest.java
│   └── search/
│       └── QcStatusSearchRequest.java
├── qcaction/
│   └── …
└── defecttype/
    └── …
```

## Dependency Flow

```
presentation → application.port.in → (domain) ← application.service
                                                        ↓
                                              domain.repository (interface)
                                                        ↑
                                          infrastructure.persistence (impl)
```

Outer layers depend on inner layers. Inner layers never import outer layers.

## Layer-by-Layer Rules

### domain/
- Pure Java only — zero framework imports (no Spring, no jOOQ, no Lombok runtime annotations)
- Entities hold business logic factory methods (e.g., `Bom.create(...)`)
- Repository interfaces define persistence contracts — implemented in infrastructure, never here

### application/
**Allowed imports:** own domain, `common/`, other modules' `port/in/` interfaces, Lombok, `@Service`, `@Transactional`

**Forbidden imports:**
| Forbidden | Reason | Solution |
|-----------|--------|----------|
| `org.springframework.security.*` | framework leak | wrap in `port/out/` interface |
| `org.jooq.*` | persistence concern | belongs in infrastructure |
| `@RestController`, `@GetMapping`, etc. | presentation concern | belongs in presentation |
| Any `*PersistenceAdapter`, `*Repository` impl | infrastructure class | depend on the interface |
| Any class from another module's `infrastructure/` | cross-layer violation | use that module's `port/in/` |

**Rules:**
- Services implement `port/in/*UseCase` and depend on `domain.repository` + `port/out/` interfaces only
- Every external framework dependency (Spring Security, email, SMS, etc.) must be wrapped in a `port/out/` interface implemented in infrastructure
- All `@Transactional` annotations live here, on service methods — never in infrastructure or presentation

### infrastructure/
- Implements interfaces from `domain.repository/` and `application/port/out/`
- Uses jOOQ `DSLContext` for all DB queries — no raw SQL strings
- `*PersistenceAdapter` extends `BaseRepository<R>` and implements `*Repository`
- `*RecordMapper` maps jOOQ generated records ↔ domain entities
- Framework-specific code (Spring Security, bcrypt, etc.) is confined here

### presentation/
- Injects use case interface (`BomUseCase`), never the concrete service (`BomService`)
- No business logic — delegates entirely to use case
- Returns `ResponseEntity<ApiResponse<T>>` for every endpoint
- Validates request bodies with `@Valid`
- Extracts `AppUserPrincipal` via `@AuthenticationPrincipal` — does NOT pass `userId` to use cases (services fetch it from `CurrentUserPort` themselves)

## Module List

```
fpt.qn.mes
├── auth/
├── bom/
├── inventory/
├── maintenance/
├── quality/
├── workorder/
├── role/
├── user/
├── master/
│   ├── line/
│   ├── location/
│   ├── machine/
│   ├── product/
│   └── warehouse/
└── common/            # Shared: ApiResponse, AppException, BaseRepository, PaginationResult
```

## Mapper Rules

There are two distinct mapper types in this project. They live in different layers and must never be mixed.

### `*DtoMapper` — application layer, MapStruct

Location: `application/mapper/`  
Purpose: map between **domain entities** and **DTOs** (request → entity, entity → response DTO)

```
Domain Entity  ←→  DTO (request/response)
```

- Must be a MapStruct `@Mapper` **interface** with `componentModel = "spring"`
- Never write manual mapping code — add `@Mapping` annotations if field names differ
- Never import jOOQ classes (`DSLContext`, records, `Tables.*`)

```java
@Mapper(componentModel = "spring")
public interface BomDtoMapper {
    BomDto toDto(Bom bom);
    Bom toDomain(CreateBomRequest request);
}
```

### `*RecordMapper` — infrastructure layer, manual

Location: `infrastructure/persistence/`  
Purpose: map between **jOOQ generated records** and **domain entities**

```
jOOQ Record  ←→  Domain Entity
```

- Must be a plain `@Component` class with manual field-by-field mapping
- Never use MapStruct — jOOQ records require explicit type conversions (e.g. `OffsetDateTime → Instant`, `UUID` nullability) that MapStruct cannot handle cleanly
- Never import DTO classes — record mappers only know about records and domain entities

```java
@Component
public class BomRecordMapper {

    public Bom toDomain(BomsRecord r) {
        return Bom.builder()
            .id(r.getId())
            .version(r.getVersion())
            .createdAt(r.getCreatedAt().toInstant())
            .build();
    }

    public BomsRecord toRecord(Bom bom) {
        BomsRecord r = new BomsRecord();
        r.setId(bom.getId());
        r.setVersion(bom.getVersion());
        r.setCreatedAt(bom.getCreatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}
```

### Summary

| | `*DtoMapper` | `*RecordMapper` |
|---|---|---|
| Layer | `application/mapper/` | `infrastructure/persistence/` |
| Type | MapStruct `interface` | `@Component` class |
| Maps | Domain ↔ DTO | jOOQ record ↔ Domain |
| Knows about DTOs | ✅ | ❌ |
| Knows about jOOQ records | ❌ | ✅ |
| Manual mapping | ❌ (use `@Mapping`) | ✅ required |

## Exception Rules

Every module must define its own exceptions under `application/exception/`. All module exceptions **must extend `AppException`** — never extend `RuntimeException` or any other base class directly.

### Structure

```
{module}/application/exception/
├── {Entity}NotFoundException.java      # 404 — entity does not exist
└── {Entity}AlreadyExistsException.java # 409 — duplicate creation
```

### Canonical patterns

```java
// 404 Not Found
public class BomNotFoundException extends AppException {
    public BomNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message);
    }
}

// 409 Conflict
public class UsernameAlreadyExistsException extends AppException {
    public UsernameAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, ErrorCode.CONFLICT, message);
    }
}

// 400 Bad Request (domain rule violation)
public class InvalidBomVersionException extends AppException {
    public InvalidBomVersionException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_INPUT, message);
    }
}
```

### Rules

- Every module defines its own exceptions — never throw a foreign module's exception
- All module exceptions extend `AppException` from `common.exception`
- Never throw `RuntimeException`, `IllegalArgumentException`, or any non-`AppException` from service layer
- Exception class names describe **what went wrong**, not the HTTP status (`BomNotFoundException`, not `NotFoundException404`)
- Exceptions are thrown from the **service layer** only — controllers never throw, infrastructure never throws business exceptions
- All exceptions are caught centrally by `GlobalExceptionHandler` — do not add `try/catch` around service calls in controllers

## Cross-Module Dependency Rules

Modules in this monolith are allowed to depend on each other — but only through **stable, controlled boundaries**. The goal is to prevent tight coupling and circular dependencies, not perfect isolation.

### Three tiers

**Tier 1 — `common/` is unconditionally shared**

Every module may import from `common`. It holds infrastructure that has no business owner:
- `AppException`, `ErrorCode`, `GlobalExceptionHandler`
- `ApiResponse`, `PageResponse`, `PaginationResult`, `BaseRepository`

Security principals (`AppUserPrincipal`, `CurrentUserPort`) live in `auth/application/` — import them from there, not from `common`.

**Tier 2 — Cross-module calls through use case interfaces only**

A module may depend on another module's `application/port/in/*UseCase` interface. It must never import the service implementation, repository, persistence adapter, or domain entity.

```java
// ALLOWED — depend on the interface
@Service
public class WorkOrderService implements WorkOrderUseCase {
    ProductUseCase productUseCase; // from master/product/application/port/in
}

// FORBIDDEN — importing the service directly
ProductService productService; // ❌

// FORBIDDEN — importing the domain entity across modules
Product product; // ❌  reference foreign data by UUID instead

// FORBIDDEN — importing another module's repository or adapter
ProductRepository productRepository; // ❌
```

**Tier 3 — No circular dependencies**

The dependency graph must be a directed acyclic graph (DAG). The allowed direction for this MES:

```
auth
role
user ──────────────────────→ role
master (product, machine, line, warehouse, location)
bom ────────────────────────→ master/product
inventory ──────────────────→ master (product, warehouse, location)
workorder ──────────────────→ master (product, line, machine), bom
quality ────────────────────→ workorder
maintenance ────────────────→ master/machine
```

### Summary table

| Allowed | Forbidden |
|---------|-----------|
| Import another module's `*UseCase` interface | Import another module's `*Service` class |
| Import from `common/` | Import another module's `*Repository` or `*PersistenceAdapter` |
| Reference foreign entities by `UUID` | Import and pass another module's domain entity |
| Depend on a module that is "upstream" in the DAG | Create a circular dependency |
