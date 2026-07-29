# Research & Decisions

## 1. Exception Type for Validation Errors
- **Decision**: Replace `IllegalArgumentException` with custom `AppException` subclasses (e.g. `InvalidStockMovementException` or `DomainException`).
- **Rationale**: Clean Architecture & project rules mandate that exceptions originating from the service layer carry an `HttpStatus` and `ErrorCode` via `AppException`.

## 2. Repository Return Types
- **Decision**: Repositories will return `List<T>` directly.
- **Rationale**: Repositories should return raw collections of domain models (`List<T>`). Converting or wrapping lists into `PageResponse` or `PaginationResult` is the responsibility of the application service layer when responding to paginated endpoint requests.
