# Backend Java Style Rules

## Lambda Expressions vs Method References

Always use **lambda expression syntax**. Method references are **not allowed**.

| Not allowed (method reference) | Required (lambda) |
|-------------------------------|-------------------|
| `list.forEach(System.out::println)` | `list.forEach(s -> System.out.println(s))` |
| `list.stream().map(String::toUpperCase)` | `list.stream().map(s -> s.toUpperCase())` |
| `list.stream().filter(Objects::nonNull)` | `list.stream().filter(o -> o != null)` |
| `list.stream().map(Bom::getId)` | `list.stream().map(b -> b.getId())` |
| `optional.ifPresent(this::process)` | `optional.ifPresent(x -> this.process(x))` |
| `stream.collect(Collectors.toList())` | `stream.collect(Collectors.toList())` *(not a method ref — fine)* |

**Why**: Lambda syntax is more explicit and readable — the parameter name provides context about what is being operated on. Method references hide the argument and make it harder to add logic later.

---

## No Java Records

**Java `record` is not allowed.** Use a regular class with Lombok instead.

```java
// FORBIDDEN
public record BomDto(UUID id, Integer version) {}

// REQUIRED
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomDto {
    UUID id;
    Integer version;
}
```

**Why**: Records are immutable and cannot be extended or annotated the same way as classes. Lombok gives the same boilerplate reduction with full flexibility — adding fields, validation annotations, or custom methods remains straightforward.

---

## Lombok Usage

Use Lombok for **all** boilerplate. Never write getters, setters, constructors, builders, or toString manually.

### Annotation by class type

| Class type | Required annotations |
|------------|---------------------|
| Domain entity | `@Getter @Builder @FieldDefaults(level = AccessLevel.PRIVATE)` |
| Response DTO | `@Getter @Builder @NoArgsConstructor @AllArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE)` |
| Request DTO | `@Getter @Setter @FieldDefaults(level = AccessLevel.PRIVATE)` |
| Service | `@Service @RequiredArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` |
| Controller | `@RestController @RequiredArgsConstructor @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` |
| Persistence adapter | `@Repository @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` |
| MapStruct mapper | no Lombok needed |

### Rules

- **Never write a constructor manually** when `@RequiredArgsConstructor`, `@AllArgsConstructor`, or `@NoArgsConstructor` covers it.
- **Never write `getX()` / `setX()` manually** — use `@Getter` / `@Setter`.
- **Never use `@Data`** on domain entities — it generates `equals`/`hashCode` based on all fields which causes issues with mutable state and JPA proxies.
- **Always use `@FieldDefaults`** instead of writing `private` / `private final` on every field.
- **Use `@Builder`** for any class that is constructed in multiple places with different field combinations.
- **Use `@Builder.Default`** for fields with default values inside a `@Builder` class (e.g., `@Builder.Default LocalDateTime timestamp = LocalDateTime.now()`).
- **Use `@Slf4j`** instead of declaring `private static final Logger log = ...` manually.

### Examples

```java
// FORBIDDEN — manual boilerplate
public class BomDto {
    private UUID id;
    private Integer version;

    public UUID getId() { return id; }
    public Integer getVersion() { return version; }

    public BomDto(UUID id, Integer version) {
        this.id = id;
        this.version = version;
    }
}

// REQUIRED — Lombok
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BomDto {
    UUID id;
    Integer version;
}
```

```java
// FORBIDDEN — manual logger
public class BomService {
    private static final Logger log = LoggerFactory.getLogger(BomService.class);
}

// REQUIRED
@Slf4j
public class BomService {
    // use log.info(...), log.warn(...), etc.
}
```

---

## Utilities

**Pagination Calculation:** Never write manual math or use `Math.ceil()` to calculate total pages for paginated responses. Always use the centralized utility method.

```java
// FORBIDDEN
int totalPages = (int) Math.ceil((double) result.getTotal() / size);

// REQUIRED
int totalPages = PaginationUtils.calculateTotalPages(result.getTotal(), size);
```
