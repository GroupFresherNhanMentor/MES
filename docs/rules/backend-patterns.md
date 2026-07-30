# Backend Code Patterns

Canonical skeletons for each layer. Follow these exactly when creating new artifacts.

## Domain Entity

Every entity with audit fields uses a `UserRef` static inner class for `createdBy`/`updatedBy`.
Factory methods accept raw UUIDs — never domain objects as parameters.

```java
// domain/entities/Product.java
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Getter
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserRef {
        UUID id;
        String fullName;
        String username;
    }

    UUID id;
    String code;
    String name;
    String version;
    ProductType productType;    // nested entity — holds full object when read from DB
    Instant createdAt;
    UserRef createdBy;
    Instant updatedAt;
    UserRef updatedBy;

    public static Product create(String code, String name, String version, UUID productTypeId, UUID createdBy) {
        return Product.builder()
            .id(UuidV7.generate())
            .code(code)
            .name(name)
            .version(version)
            .productType(ProductType.builder().id(productTypeId).build())  // stub with id only
            .createdBy(UserRef.builder().id(createdBy).build())            // stub with id only
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    public static Product update(Product existing, String name, UUID updatedBy) {
        return Product.builder()
            .id(existing.id)
            .code(existing.code)
            .name(name != null ? name : existing.name)
            .version(existing.version)
            .productType(existing.productType)
            .createdAt(existing.createdAt)
            .createdBy(existing.createdBy)
            .updatedAt(Instant.now())
            .updatedBy(UserRef.builder().id(updatedBy).build())
            .build();
    }
}
```

**Rules:**
- Factory methods (`create`, `update`, `changeStatus`) take **UUIDs**, never domain entity objects
- Nested FK entities are stubs on write: `ProductType.builder().id(productTypeId).build()`
- `createdBy` is a `UserRef` stub on create (only `id` known at write time; full data comes back on read)
- `updatedBy` starts `null` on create — the DB column must be nullable

## Domain Repository Interface

```java
// domain/repository/ProductRepository.java
public interface ProductRepository {
    Optional<Product> findById(UUID id);
    Product save(Product product);
    Product update(Product product);
    PaginationResult<Product> search(ProductSearchCriteria criteria);
    boolean existsByCode(String code);
    boolean existsByVersion(String version);
    boolean existsById(UUID id);
}
```

**Rules:**
- Use `search(criteria)` — never `findAll(page, size)` with raw ints
- Expose `existsById` / `existsByCode` / `existsByName` for validation — avoids loading a full entity just to confirm it exists
- `findById` is only for when you need the entity itself (e.g. to run business logic on it)

## Search Criteria & Request

Both always extend the base classes — never declare `page`, `size`, or `sort` directly.

```java
// domain/repository/criteria/ProductSearchCriteria.java
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchCriteria extends BaseSearchCriteria {
    String code;
    String name;
    UUID productTypeId;
}

// application/dto/product/search/ProductSearchRequest.java
@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSearchRequest extends BaseSearchRequest {
    String code;
    String name;
    UUID productTypeId;
}
```

`BaseSearchCriteria` provides `page`, `size`, `List<String> sort`.
`BaseSearchRequest` provides `page`, `size`, `List<String> sort` with validation.

## Use Case Interface

```java
// application/port/in/ProductUseCase.java
public interface ProductUseCase {
    PageResponse<ProductResponse> getProducts(ProductSearchRequest request);
    ProductResponse getProductById(UUID id);
    void createProduct(CreateProductRequest request);
    void updateProduct(UUID id, UpdateProductRequest request);
    void activateProduct(UUID id);
    void deactivateProduct(UUID id);
}
```

## Service

```java
// application/service/ProductService.java
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService implements ProductUseCase {

    ProductRepository productRepository;
    ProductTypeRepository productTypeRepository;
    ProductDtoMapper mapper;
    CurrentUserPort currentUserPort;

    @Override
    @Transactional
    public void createProduct(CreateProductRequest request) {
        // uniqueness checks first
        if (productRepository.existsByCode(request.getCode())) {
            throw new ProductConflictException("Product code already exists: " + request.getCode());
        }
        // FK reference validation via existsById — not findById
        if (!productTypeRepository.existsById(request.getProductTypeId())) {
            throw new ProductTypeNotFoundException("Product type not found: " + request.getProductTypeId());
        }
        UUID currentUserId = currentUserPort.getCurrentUserId();
        productRepository.save(Product.create(request.getCode(), request.getName(),
            request.getVersion(), request.getProductTypeId(), currentUserId));
    }

    @Override
    @Transactional
    public void updateProduct(UUID id, UpdateProductRequest request) {
        // findById only when you need the entity
        var existing = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        UUID currentUserId = currentUserPort.getCurrentUserId();
        productRepository.update(Product.update(existing, request.getName(), currentUserId));
    }
}
```

**Validation rules in service:**
- Use `existsByCode` / `existsByName` / `existsByVersion` for uniqueness → throw `*ConflictException` (409)
- Use `existsById` for FK reference checks → throw `*NotFoundException` (404)
- Use `findById` only when you need the full entity to pass to a factory method or run business logic
- Never call `findById` just to confirm existence

## Module Exception

```java
// application/exception/ProductNotFoundException.java
public class ProductNotFoundException extends AppException {
    public ProductNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message);
    }
}

// application/exception/ProductConflictException.java
public class ProductConflictException extends AppException {
    public ProductConflictException(String message) {
        super(HttpStatus.CONFLICT, ErrorCode.CONFLICT, message);
    }
}
```

## Response DTO with Audit User Info

Response DTOs use a `UserInfo` static inner class that mirrors the domain `UserRef`.
Never share a `UserInfo` class across modules — each module defines its own.

```java
// application/dto/product/ProductResponse.java
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserInfo {
        UUID id;
        String fullName;
        String username;
    }

    UUID id;
    String code;
    String name;
    Instant createdAt;
    UserInfo createdBy;
    Instant updatedAt;
    UserInfo updatedBy;
}
```

## DTO Mapper — MapStruct (application layer)

When the domain entity has a `UserRef`, add an explicit `toUserInfo` method so MapStruct can map it.

```java
// application/mapper/ProductDtoMapper.java
@Mapper(componentModel = "spring")
public interface ProductDtoMapper {
    ProductResponse toDto(Product product);
    ProductResponse.UserInfo toUserInfo(Product.UserRef ref);  // explicit — MapStruct uses this for UserRef fields
}
```

## Record Mapper — manual (infrastructure layer)

When the entity has audit user fields, the mapper accepts typed `UsersRecord` parameters from the JOIN.

```java
// infrastructure/persistence/product/ProductRecordMapper.java
@Component
public class ProductRecordMapper {

    public Product toDomain(ProductsRecord r, UsersRecord creator, UsersRecord updater) {
        return Product.builder()
            .id(r.getId())
            .code(r.getCode())
            .createdAt(r.getCreatedAt().toInstant())           // NOT NULL column — no null check
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toInstant() : null)  // nullable
            .createdBy(creator.getId() != null                 // LEFT JOIN: check .getId(), not object
                ? Product.UserRef.builder()
                    .id(creator.getId())
                    .fullName(creator.getFullName())
                    .username(creator.getUsername())
                    .build()
                : null)
            .updatedBy(updater.getId() != null
                ? Product.UserRef.builder()
                    .id(updater.getId())
                    .fullName(updater.getFullName())
                    .username(updater.getUsername())
                    .build()
                : null)
            .build();
    }

    public ProductsRecord toRecord(Product p) {
        ProductsRecord r = new ProductsRecord();
        r.setId(p.getId());
        r.setCode(p.getCode());
        r.setCreatedAt(OffsetDateTime.ofInstant(p.getCreatedAt(), ZoneOffset.UTC));  // always set
        r.setUpdatedAt(p.getUpdatedAt() != null ? OffsetDateTime.ofInstant(p.getUpdatedAt(), ZoneOffset.UTC) : null);
        r.setCreatedBy(p.getCreatedBy() != null ? p.getCreatedBy().getId() : null);  // UserRef may be null for seeded data
        r.setUpdatedBy(p.getUpdatedBy() != null ? p.getUpdatedBy().getId() : null);
        return r;
    }
}
```

### Null check rules

| Scenario | Rule |
|---|---|
| `r.into(TABLE)` result | Never Java null — never write `record != null`, only `record.getId() != null` |
| LEFT JOIN field (from `r.into(ALIAS)`) | Check `record.getId() != null` to detect no-match row |
| NOT NULL DB column (e.g. `created_at`) | No null guard in `toDomain` or `toRecord` |
| Nullable DB column (e.g. `updated_at`) | Keep null guard in both directions |
| Required domain field validated at controller | No null guard in `toRecord` (e.g. `p.getProductType().getId()`) |
| `UserRef` / `updatedBy` fields | Always null-guard in `toRecord` — may be null on first create |

## Persistence Adapter

One subfolder per entity: `infrastructure/persistence/{entity}/`.
No helper methods (`baseSelect`, `mapRow`, `buildSort`) — write joins inline in each method.

```java
// infrastructure/persistence/product/ProductPersistenceAdapter.java
@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductPersistenceAdapter extends BaseRepository<ProductsRecord> implements ProductRepository {

    private static final Users CREATOR = USERS.as("creator");
    private static final Users UPDATER = USERS.as("updater");

    private static final Map<String, Field<?>> SORT_FIELDS = Map.of(
        "name",       PRODUCTS.NAME,
        "created_at", PRODUCTS.CREATED_AT
    );
    private static final Field<?> DEFAULT_SORT_FIELD = PRODUCTS.CREATED_AT;

    ProductRecordMapper mapper;

    public ProductPersistenceAdapter(DSLContext ctx, ProductRecordMapper mapper) {
        super(ctx, PRODUCTS);     // BaseRepository stores ctx — use this.ctx, never redeclare dslCtx
        this.mapper = mapper;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return ctx.select()
            .from(PRODUCTS)
            .leftJoin(CREATOR).on(PRODUCTS.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCTS.UPDATED_BY.eq(UPDATER.ID))
            .where(PRODUCTS.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r.into(PRODUCTS), r.into(CREATOR), r.into(UPDATER)));
    }

    @Override
    public boolean existsById(UUID id) {
        return ctx.fetchExists(PRODUCTS, PRODUCTS.ID.eq(id));
    }

    @Override
    public boolean existsByCode(String code) {
        return ctx.fetchExists(PRODUCTS, PRODUCTS.CODE.eq(code));
    }

    @Override
    public Product save(Product product) {
        ProductsRecord r = mapper.toRecord(product);
        ctx.insertInto(PRODUCTS).set(r)
            .onConflict(PRODUCTS.ID).doUpdate().set(r)
            .execute();
        return product;   // return the passed entity — never re-fetch after save
    }

    @Override
    public PaginationResult<Product> search(ProductSearchCriteria criteria) {
        Condition condition = buildCondition(criteria);
        List<SortField<?>> orderBy = SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD);
        long total = ctx.fetchCount(PRODUCTS, condition);
        List<Product> items = ctx.select()
            .from(PRODUCTS)
            .leftJoin(CREATOR).on(PRODUCTS.CREATED_BY.eq(CREATOR.ID))
            .leftJoin(UPDATER).on(PRODUCTS.UPDATED_BY.eq(UPDATER.ID))
            .where(condition)
            .orderBy(orderBy)
            .limit(criteria.getSize())
            .offset((long) criteria.getPage() * criteria.getSize())
            .fetch(r -> mapper.toDomain(r.into(PRODUCTS), r.into(CREATOR), r.into(UPDATER)));
        return PaginationResult.<Product>builder().total(total).items(items).build();
    }

    private Condition buildCondition(ProductSearchCriteria criteria) {
        Condition condition = DSL.noCondition();
        if (criteria.getCode() != null && !criteria.getCode().isBlank()) {
            condition = condition.and(PRODUCTS.CODE.containsIgnoreCase(criteria.getCode()));
        }
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            condition = condition.and(PRODUCTS.NAME.containsIgnoreCase(criteria.getName()));
        }
        return condition;
    }
}
```

**Rules:**
- `USERS.as("creator")` / `USERS.as("updater")` as `private static final` class constants
- Use `r.into(TABLE)` / `r.into(ALIAS)` to extract typed records from a multi-join result
- `ctx` comes from `BaseRepository` — never assign it to a separate `dslCtx` field
- `SortUtils.resolveSorts(criteria.getSort(), SORT_FIELDS, DEFAULT_SORT_FIELD)` for sorting
- `DSL.noCondition()` + `.and()` chaining for condition building — no `List<Condition>` + reduce
- `save()` returns the passed entity directly — no re-fetch
- Count separately with `ctx.fetchCount(TABLE, condition)` — never `.fetch().size()`

## REST Controller

```java
// presentation/ProductController.java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductUseCase productUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @ModelAttribute ProductSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productUseCase.getProducts(request), "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productUseCase.getProductById(id), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        productUseCase.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        productUseCase.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Updated"));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateProduct(@PathVariable UUID id) {
        productUseCase.activateProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Activated"));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateProduct(@PathVariable UUID id) {
        productUseCase.deactivateProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated"));
    }
}
```

**Rules:**
- Use `@ModelAttribute` for search/filter requests (GET with query params) — not `@RequestParam` per field
- Use `@RequestBody` + `@Valid` for create/update (POST/PUT)
- All void use-case methods return `ApiResponse.success("Message")` — never pass `null` as data
- Status 201 for create, 200 for everything else
