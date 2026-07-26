# Backend Naming Conventions

## Class Naming by Layer

| Artifact | Pattern | Example |
|----------|---------|---------|
| Domain entity | `{Entity}` | `Bom`, `BomItem`, `Role` |
| Domain repository | `{Entity}Repository` | `BomRepository` |
| Use case interface | `{Entity}UseCase` | `BomUseCase` |
| Service | `{Entity}Service` | `BomService` |
| Response DTO | `{Entity}Dto` | `BomDto`, `BomItemDto` |
| Create request | `Create{Entity}Request` | `CreateBomRequest` |
| Update request | `Update{Entity}Request` | `UpdateBomRequest` |
| DTO mapper | `{Entity}DtoMapper` | `BomDtoMapper` |
| Persistence adapter | `{Entity}PersistenceAdapter` | `BomPersistenceAdapter` |
| Record mapper | `{Entity}RecordMapper` | `BomRecordMapper` |
| Controller | `{Entity}Controller` | `BomController` |
| Not-found exception | `{Entity}NotFoundException` | `BomNotFoundException` |

## Package Naming

```
fpt.qn.mes.{module}.domain.entities
fpt.qn.mes.{module}.domain.repository
fpt.qn.mes.{module}.application.port.in
fpt.qn.mes.{module}.application.service
fpt.qn.mes.{module}.application.dto
fpt.qn.mes.{module}.application.mapper
fpt.qn.mes.{module}.application.exception
fpt.qn.mes.{module}.infrastructure.persistence
fpt.qn.mes.{module}.presentation
```

## REST Endpoint Naming

- Collection resource: `GET /api/{module}s` (plural, lowercase, kebab-case)
- Single resource: `GET /api/{module}s/{id}`
- Create: `POST /api/{module}s`
- Update: `PUT /api/{module}s/{id}`
- Partial update: `PATCH /api/{module}s/{id}`
- Delete: `DELETE /api/{module}s/{id}`
- Sub-resources: `POST /api/boms/{bomId}/items`

## Field & Method Naming

- Use `UUID id` for all entity primary keys
- Use `Instant` for timestamps (`createdAt`, `updatedAt`)
- Repository methods: `findById`, `findAll`, `save`, `deleteById`
- Use case methods: describe the action (`getBomById`, `createBom`, `deleteBom`)
- Boolean fields: `is` prefix (`isActive`, `isDeleted`)

## Error Code Naming

Format: `{MODULE}_{ENTITY}_{REASON}` in SCREAMING_SNAKE_CASE

Examples: `BOM_NOT_FOUND`, `ROLE_ALREADY_EXISTS`, `USER_INVALID_CREDENTIALS`
