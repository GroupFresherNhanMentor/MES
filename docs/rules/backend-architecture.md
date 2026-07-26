# Backend Architecture Rules

## Layer Structure

Every domain module follows this exact 4-layer structure. No exceptions.

```
{module}/
├── domain/
│   ├── entities/           # Pure Java domain objects — NO framework imports
│   └── repository/         # Repository interfaces (output ports)
├── application/
│   ├── port/in/            # Use case interfaces (input ports)
│   ├── service/            # Implements use case interfaces
│   ├── dto/                # Request/Response DTOs
│   ├── mapper/             # MapStruct domain ↔ DTO mappers
│   └── exception/          # Module-scoped exceptions (extend AppException)
├── infrastructure/
│   └── persistence/        # jOOQ adapters + record mappers
└── presentation/           # Spring MVC REST controllers
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
- Pure Java only — no Spring, no jOOQ, no Lombok runtime annotations
- Entities hold business logic methods (e.g., `Bom.create(...)`)
- Repository interfaces define persistence contracts — implemented in infrastructure, not here

### application/
- No Spring MVC annotations (`@RestController`, `@GetMapping`, etc.)
- No jOOQ imports (`DSLContext`, `Tables.*`, jOOQ records)
- No infrastructure classes
- Services depend on `domain.repository` interfaces, never on adapters directly
- All `@Transactional` annotations live here, on service methods

### infrastructure/
- Implements `domain.repository.*Repository` interfaces
- Uses jOOQ `DSLContext` for all queries
- `*PersistenceAdapter` extends `BaseRepository<R>` and implements `*Repository`
- `*RecordMapper` maps jOOQ generated records ↔ domain entities

### presentation/
- Injects use case interface (`BomUseCase`), never the concrete service (`BomService`)
- No business logic — delegates entirely to use case
- Returns `ResponseEntity<ApiResponse<T>>` for every endpoint
- Validates request bodies with `@Valid`

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

## Cross-Cutting Rules

- Business exceptions are thrown from the **service layer**, caught by `GlobalExceptionHandler`
- Never throw `RuntimeException` directly — always use `AppException(HttpStatus, errorCode, message)`
- The `common/` module is the only shared layer — modules must NOT import from each other
