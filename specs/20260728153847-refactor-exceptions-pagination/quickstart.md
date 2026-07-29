# Validation Quickstart

This document explains how to validate that the exception and pagination refactoring has been successfully applied.

## 1. Run Unit and Integration Tests
Since the project relies heavily on its test suite to validate API responses and database queries, running the entire backend test suite is the primary validation step.

```bash
cd be
./mvnw clean test
```
**Expected Outcome**: All tests pass. Existing tests for validation failures should now explicitly assert for `400 Bad Request` and `INVALID_INPUT` instead of `500 Internal Server Error`.

## 2. Manual API Testing (Optional)
If you want to manually verify the exception change:

1. Start the application:
   ```bash
   cd be
   ./mvnw spring-boot:run
   ```
2. Send an API request that deliberately triggers a domain validation error (e.g., creating a stock movement with invalid negative quantities).
3. **Expected Outcome**: The response should be a JSON error object:
   ```json
   {
     "success": false,
     "errorCode": "INVALID_INPUT",
     "message": "Invalid stock movement: negative quantity not allowed",
     "timestamp": "..."
   }
   ```
   The HTTP status code must be `400 Bad Request`.
