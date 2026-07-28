# Data Model

Since this is an architectural refactoring focusing on exception handling and repository interfaces, no changes are being made to the database schema or the domain entities themselves.

## Modified Interfaces

- **Repositories**: Method signatures changing from `PageResponse<T>` to `List<T>` or `PaginationResult<T>`.
- **Services**: Will map `PaginationResult<T>` to `PageResponse<Dto>`.
- **Exceptions**: New `DomainException` extending `AppException`.
