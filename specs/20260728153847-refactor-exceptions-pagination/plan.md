# Implementation Plan: Exception & List-Only Repository Refactoring

**Branch**: `20260728153847-refactor-exceptions-pagination` | **Date**: 2026-07-28 | **Spec**: [spec.md](spec.md)

## Summary

Refactor error handling to use `AppException` (400 Bad Request) instead of `IllegalArgumentException`. Refactor all repository `search` / `find` methods in the inventory module to return `List<T>` directly. Service layer will construct `PageResponse` when needed.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.1.0, jOOQ 3.21
**Storage**: PostgreSQL 18
**Testing**: JUnit 5, Mockito

## Constitution Check

- [x] Service layer throws `AppException` variants.
- [x] Repositories return `List<T>` directly.
- [x] Service layer converts `List<T>` to `PageResponse<Dto>` for presentation.

## Project Structure

### Documentation (this feature)

```text
specs/20260728153847-refactor-exceptions-pagination/
├── plan.md              
├── research.md          
├── data-model.md        
├── quickstart.md        
└── tasks.md             
```
