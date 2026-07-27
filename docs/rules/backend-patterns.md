# Backend Code Patterns

Canonical skeletons for each layer. Follow these exactly when creating new artifacts.

## Domain Entity

```java
// domain/entities/Bom.java
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bom {
    UUID id;
    UUID finishedProductId;
    Integer version;
    UUID bomStatusId;
    UUID createdBy;
    Instant createdAt;
    List<BomItem> items;

    public static Bom create(UUID finishedProductId, Integer version, UUID bomStatusId, UUID createdBy) {
        return Bom.builder()
            .id(UUID.randomUUID())
            .finishedProductId(finishedProductId)
            .version(version)
            .bomStatusId(bomStatusId)
            .createdBy(createdBy)
            .createdAt(Instant.now())
            .items(new ArrayList<>())
            .build();
    }
}
```

## Domain Repository Interface

```java
// domain/repository/BomRepository.java
public interface BomRepository {
    Optional<Bom> findById(UUID id);
    Bom save(Bom bom);
    void deleteById(UUID id);
    PaginationResult<Bom> findAll(int page, int size);
}
```

## Use Case Interface

```java
// application/port/in/BomUseCase.java
public interface BomUseCase {
    PageResponse<BomDto> getBoms(int page, int size);
    BomDto getBomById(UUID id);
    BomDto createBom(CreateBomRequest request);  // service fetches current user itself
    void deleteBom(UUID id);
}
```

## Output Port (external dependency interface)

```java
// application/port/out/BomExternalPort.java  (example)
public interface SomeExternalPort {
    void doSomething(String param);
}
```

For auth specifically, the pre-built ports are:
- `CurrentUserPort` — `getCurrentUser()`, `getCurrentUserId()`, `getCurrentUsername()`
- `PasswordPort` — `encode(raw)`, `matches(raw, encoded)`
- `TokenPort` — `generateAccessToken(...)`, `generateRefreshToken(...)`

## Service

```java
// application/service/BomService.java
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomService implements BomUseCase {

    BomRepository bomRepository;      // domain interface — never the adapter
    BomDtoMapper mapper;
    CurrentUserPort currentUserPort;  // auth output port — never SecurityUtil directly

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BomDto> getBoms(int page, int size) {
        PaginationResult<Bom> result = bomRepository.findAll(page, size);
        return PageResponse.of(
            result.items().stream().map(b -> mapper.toDto(b)).toList(),
            result.total(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public BomDto getBomById(UUID id) {
        return bomRepository.findById(id)
            .map(b -> mapper.toDto(b))
            .orElseThrow(() -> new BomNotFoundException("BOM not found: " + id));
    }

    @Override
    @Transactional
    public BomDto createBom(CreateBomRequest request) {
        UUID currentUserId = currentUserPort.getCurrentUserId();  // fetched here, not passed from controller
        Bom bom = Bom.create(request.getFinishedProductId(), request.getVersion(),
            request.getBomStatusId(), currentUserId);
        return mapper.toDto(bomRepository.save(bom));
    }

    @Override
    @Transactional
    public void deleteBom(UUID id) {
        if (bomRepository.findById(id).isEmpty()) throw new BomNotFoundException("BOM not found: " + id);
        bomRepository.deleteById(id);
    }
}
```

## Module Exception

```java
// application/exception/BomNotFoundException.java
public class BomNotFoundException extends AppException {
    public BomNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, message);
    }
}
```

## DTO Mapper — MapStruct (application layer)

```java
// application/mapper/BomDtoMapper.java
@Mapper(componentModel = "spring")
public interface BomDtoMapper {
    BomDto toDto(Bom bom);

    // field name differs between request and entity → use @Mapping
    @Mapping(target = "bomStatusId", source = "statusId")
    Bom toDomain(CreateBomRequest request);
}
```

## Record Mapper — manual (infrastructure layer)

```java
// infrastructure/persistence/BomRecordMapper.java
@Component
public class BomRecordMapper {

    public Bom toDomain(BomsRecord r) {
        return Bom.builder()
            .id(r.getId())
            .finishedProductId(r.getFinishedProductId())
            .version(r.getVersion())
            .createdAt(r.getCreatedAt().toInstant())
            .build();
    }

    public BomsRecord toRecord(Bom bom) {
        BomsRecord r = new BomsRecord();
        r.setId(bom.getId());
        r.setFinishedProductId(bom.getFinishedProductId());
        r.setVersion(bom.getVersion());
        r.setCreatedAt(bom.getCreatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}
```

## Persistence Adapter

```java
// infrastructure/persistence/BomPersistenceAdapter.java
@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomPersistenceAdapter extends BaseRepository<BomsRecord> implements BomRepository {

    BomRecordMapper mapper;
    DSLContext dslCtx;

    public BomPersistenceAdapter(DSLContext ctx, BomRecordMapper mapper) {
        super(ctx, BOMS);
        this.mapper = mapper;
        this.dslCtx = ctx;
    }

    @Override
    public Optional<Bom> findById(UUID id) {
        return dslCtx.selectFrom(BOMS)
            .where(BOMS.ID.eq(id))
            .fetchOptional(r -> mapper.toDomain(r));
    }

    @Override
    public Bom save(Bom bom) {
        BomsRecord record = mapper.toRecord(bom);
        dslCtx.insertInto(BOMS).set(record)
            .onConflict(BOMS.ID).doUpdate().set(record)
            .execute();
        return bom;
    }

    @Override
    public void deleteById(UUID id) {
        dslCtx.deleteFrom(BOMS).where(BOMS.ID.eq(id)).execute();
    }

    @Override
    public PaginationResult<Bom> findAll(int page, int size) {
        int offset = page * size;
        List<Bom> items = dslCtx.selectFrom(BOMS)
            .limit(size).offset(offset)
            .fetch(r -> mapper.toDomain(r));
        int total = dslCtx.fetchCount(BOMS);
        return new PaginationResult<>(items, total);
    }
}
```

## REST Controller

```java
// presentation/BomController.java
@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BomController {

    BomUseCase bomUseCase;   // interface — never BomService directly

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BomDto>>> getBoms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(bomUseCase.getBoms(page, size), "OK"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BomDto>> getBomById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bomUseCase.getBomById(id), "OK"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BomDto>> createBom(
            @Valid @RequestBody CreateBomRequest request) {
        // do NOT extract userId here — the service fetches it from CurrentUserPort
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(bomUseCase.createBom(request), "Created"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBom(@PathVariable UUID id) {
        bomUseCase.deleteBom(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
```
