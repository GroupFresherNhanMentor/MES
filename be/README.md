# Spring Boot Boilerplate

A production-ready Spring Boot boilerplate with Clean Architecture, JWT authentication, jOOQ, Flyway, and Testcontainers.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.0 |
| Architecture | Clean Architecture |
| Database | PostgreSQL 18 |
| Query Builder | jOOQ 3.21 |
| Migration | Flyway |
| Security | Spring Security + JWT (OAuth2 Resource Server) |
| Mapping | MapStruct 1.6.3 + Lombok |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Testcontainers |

---

## Project Structure

```
bvannthang.boilerplate
├── common/
│   ├── dto/            # ApiResponse, PageResponse, PaginationResult
│   ├── exception/      # AppException, GlobalExceptionHandler
│   └── repository/     # BaseRepository, Repository interface
│
├── config/             # SecurityConfig
│
├── auth/
│   └── adapter/
│       └── out/security/   # JwtTokenProvider, UserDetailsServiceImpl
│
└── user/
    ├── application/
    │   ├── dto/            # UserDto, CreateUserRequest
    │   ├── port/
    │   │   ├── in/         # UserUseCase (input port)
    │   │   └── out/        # UserPort (output port)
    │   └── service/        # UserService
    └── adapter/
        ├── in/web/         # UserController
        └── out/persistence/ # UserPersistenceAdapter, UserMapper
```

### Clean Architecture Dependency Flow

```
UserController → UserUseCase ← UserService → UserPort ← UserPersistenceAdapter
   (adapter)      (port/in)    (service)     (port/out)      (adapter)
```

The application layer (`port/`, `service/`) has zero dependency on Spring MVC, jOOQ, or any infrastructure framework.

---

## Getting Started

### Prerequisites

- JDK 25
- Docker (for PostgreSQL via Docker Compose)
- Maven 3.9+ or use the included `./mvnw` wrapper

### Run locally

1. **Start PostgreSQL:**
   ```bash
   docker compose up -d
   ```

2. **Generate jOOQ sources and build:**
   ```bash
   chmod +x mvnw
   ./mvnw clean package -DskipTests
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Swagger UI:**
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

### Environment variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/db` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | *(see application.yaml)* | HMAC-SHA256 secret key |
| `JWT_ACCESS_EXPIRATION` | `900000` | Access token TTL (ms) |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Refresh token TTL (ms) |

---

## Adding a New Module

Follow this structure for each new feature module:

```
{module}/
├── application/
│   ├── dto/            # Request / Response DTOs
│   ├── port/
│   │   ├── in/         # Use case interfaces
│   │   └── out/        # Persistence / external port interfaces
│   └── service/        # Use case implementations
└── adapter/
    ├── in/web/         # REST controllers
    └── out/persistence/ # Persistence adapters + mappers
```

Rules:
- `application/` must not import from `adapter/`
- `adapter/` depends on `application/port` interfaces only
- All API responses are wrapped in `ApiResponse<T>`
- DB schema changes go in `src/main/resources/db/migration/` as Flyway versioned scripts
