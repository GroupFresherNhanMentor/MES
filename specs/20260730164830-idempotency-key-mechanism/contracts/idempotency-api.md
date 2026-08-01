# Contract: Idempotency Key HTTP Header Interface

## Overview

The idempotency key mechanism transparently intercepts state-mutating HTTP requests containing the `X-Idempotency-Key` header.

## Request Header

| Header Name | Required | Type | Example | Description |
|-------------|----------|------|---------|-------------|
| `X-Idempotency-Key` | Optional | String (UUID / alphanumeric string) | `7b1450a2-03e3-4bdf-99d1-beb24f75ddd8` | Unique client-generated key for request deduplication. |

---

## Behavior Matrix

| Client Request Scenario | HTTP Response Status Code | Response Body Source | System Action |
|-------------------------|---------------------------|----------------------|---------------|
| First request with new key | 200 OK / 201 Created | Executed Service Output | Intercepts request, executes business logic, stores response code and payload in DB. |
| Duplicate request with completed key | Same as cached status (e.g. 200 / 201) | Cached JSON Payload from DB | Bypasses business logic execution, immediately returns stored status and payload. |
| Concurrent duplicate request (key processing in progress) | 409 Conflict | Standard Error Response | Rejects concurrent duplicate request to prevent race condition. |
| Same key reused with different payload | 400 Bad Request | Standard Error Response | Detects payload hash mismatch (`request_hash`), rejects request execution. |
