# Specification: Exception and Repository Return Type Refactoring

## 1. Feature Description
This feature focuses on cleaning up architectural debt by aligning error handling and repository response types with Clean Architecture guidelines. Specifically, it involves migrating generic `IllegalArgumentException` usages to domain-specific `AppException`s, and changing repository method return types to return raw `List<T>` directly instead of presentation or pagination wrappers (`PageResponse<T>` or `PaginationResult<T>`).

## 2. User Scenarios & Testing

### 2.1 Scenario 1: API Error Responses
- **Given:** A client sends a request that violates a domain rule (e.g., negative stock quantity)
- **When:** The system processes the request and encounters a validation failure
- **Then:** The client receives a well-formatted 400 Bad Request JSON response containing the proper `errorCode` (e.g., `INVALID_INPUT`) instead of a generic 500 Internal Server Error.

### 2.2 Scenario 2: Data Retrieval from Repository
- **Given:** A service queries a repository search or list method
- **When:** The repository fetches items from the database
- **Then:** The repository returns a `List<T>` directly. The service layer handles any necessary pagination wrapper or conversion to `PageResponse` if returning to a controller.

## 3. Functional Requirements
- **FR-1:** Replace all instances of `IllegalArgumentException` (and similar generic exceptions) thrown from the Service layer with custom exceptions extending `AppException` (e.g., `DomainException` or `InvalidStockMovementException`).
- **FR-2:** Configure the new exceptions to return an appropriate HTTP status (e.g., 400 Bad Request) and ErrorCode (e.g., `INVALID_INPUT`).
- **FR-3:** Modify all Repository interfaces and their implementations in the `inventory` module to return `List<T>` instead of `PageResponse<T>` or `PaginationResult<T>`.
- **FR-4:** Update the Service layer to map the Repository's `List<T>` into a `PageResponse<Dto>` before returning it to the Controller.

## 4. Success Criteria
- **SC-1:** 100% of domain validation errors return a well-formatted `ApiResponse` JSON with a 400 status code rather than a generic 500 error.
- **SC-2:** The `PageResponse` and `PaginationResult` classes are no longer imported or referenced within the `inventory` repository interfaces or persistence adapters.
- **SC-3:** All existing integration and unit tests pass after the refactoring.

## 5. Assumptions & Exclusions

### Assumptions
- The global exception handler is already configured to properly handle exceptions extending `AppException`.

### Out of Scope
- Adding new functional features to the stock movement module.
- Refactoring database tables.
