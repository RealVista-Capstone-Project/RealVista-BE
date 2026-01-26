# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

RealVista Backend is a Spring Boot 3.5.9 application using Java 21 that implements Clean Architecture with Domain-Driven Design (DDD) principles. The application provides JWT-based authentication, user management, and a RESTful API with comprehensive CI/CD pipeline.

## Essential Commands

### Build & Run
```bash
# Start PostgreSQL database (required for local development)
docker-compose up -d

# Full build with tests
mvn clean verify

# Build only (skip tests)
mvn clean install -DskipTests

# Run application locally
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserControllerComponentTest

# Run with coverage report
mvn test jacoco:report

# Run integration tests with Testcontainers
mvn verify
```

### Code Quality
```bash
# Run all quality checks
mvn clean verify

# Individual checks
mvn checkstyle:check          # Code style enforcement
mvn spotbugs:check            # Static analysis
mvn jacoco:report             # Coverage report (target/site/jacoco/index.html)
```

### Database Operations
```bash
# Run Flyway migrations
mvn flyway:migrate

# Validate migrations
mvn flyway:validate

# Check migration status
mvn flyway:info
```

## Architecture Overview

The codebase follows **Clean Architecture** with four distinct layers. Dependencies flow inward - Domain layer has zero dependencies on outer layers.

### Layer Structure

```
src/main/java/com/sep/realvista/
├── domain/              # Core business logic (no external dependencies)
│   ├── user/           # User domain: entity, repository interface, domain service
│   └── common/         # Shared: BaseEntity, value objects (Email), exceptions
├── application/         # Use cases & orchestration
│   ├── user/           # UserApplicationService, DTOs, mappers
│   └── auth/           # AuthService, authentication DTOs
├── infrastructure/      # Technical implementation
│   ├── config/         # Security, JPA, OpenAPI configs
│   ├── persistence/    # Repository implementations (UserRepositoryImpl)
│   └── security/       # JWT, CustomUserDetailsService
└── presentation/        # REST API layer
    └── rest/           # Controllers (UserController, AuthenticationController)
```

### Key Architectural Patterns

1. **Repository Pattern**: Domain defines `UserRepository` interface; Infrastructure implements it via `UserRepositoryImpl` using Spring Data JPA
2. **DTO Pattern**: Application layer uses MapStruct mappers to convert between entities and DTOs
3. **Service Pattern**:
   - `*ApplicationService` classes orchestrate use cases and transactions
   - `*DomainService` classes contain business logic that doesn't fit in entities
4. **Soft Delete**: All entities extend `BaseEntity` with `deleted` flag - never hard-delete records

### Domain Entity Example

User entity (`domain/user/User.java`):
- Contains business logic methods: `activate()`, `suspend()`, `updateProfile()`, `updatePassword()`
- Uses `@Embedded` value objects like `Email` for validation
- Builder pattern with Lombok `@Builder`
- Extends `BaseEntity` for auditing (`createdAt`, `updatedAt`, `deleted`)

## Configuration Management

### Environment Profiles
- **dev**: Development with PostgreSQL at localhost:5432
- **test**: Testing with H2 in-memory database (no Docker required)
- **prod**: Production settings

### Environment Variables
Key variables (see `application.yml`):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION_MS`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` (OAuth2)
- `SERVER_PORT`, `SPRING_PROFILES_ACTIVE`

Configuration uses `spring-dotenv` to load `.env` files. Create a `.env` file in the project root for local overrides.

### Test Configuration
Tests use H2 in-memory database with PostgreSQL compatibility mode. Flyway migrations run automatically. Test-specific config in `src/test/resources/application-test.yml`.

## Testing Strategy

### Test Organization
```
src/test/java/com/sep/realvista/
├── unit/               # Unit tests (mocked dependencies)
│   ├── application/
│   ├── domain/
│   └── infrastructure/
└── component/          # Integration tests (full Spring context)
    └── presentation/
```

### Test Patterns

**Unit Tests**: Use `@ExtendWith(MockitoExtension.class)` - test business logic in isolation
**Component Tests**: Use `@SpringBootTest` - test full HTTP request/response cycle

### Key Testing Notes
- Tests use H2 database by default (configured in `application-test.yml`)
- For integration tests with real PostgreSQL, use `@Testcontainers` (available as dependency)
- Current coverage target: 70% (enforced in CI but not in build)
- Use `@Transactional` for tests that modify data

## Security Implementation

### Authentication Flow
1. JWT-based stateless authentication
2. `JwtAuthenticationFilter` intercepts requests and validates tokens
3. `JwtService` generates and validates JWT tokens
4. `CustomUserDetailsService` loads user from database
5. OAuth2 support for Google authentication

### Security Config
- Public endpoints defined in `SecurityConstants.PublicEndpoints.PUBLIC_PATHS`
- Role-based access control using `@PreAuthorize` annotations
- Passwords hashed with BCrypt
- Google OAuth2 integration with redirect at `/oauth2/authorization/google`

### Key Security Classes
- `SecurityConfig.java`: Main security configuration
- `JwtService.java`: Token generation and validation
- `JwtAuthenticationFilter.java`: Request interceptor
- `OAuth2AuthenticationSuccessHandler.java`: OAuth2 callback handler

## Common Development Patterns

### Adding a New Domain Entity

Follow this sequence to maintain Clean Architecture:

1. **Domain Layer** (`domain/newentity/`)
   - Create entity extending `BaseEntity`
   - Create repository interface
   - Create domain service if needed
   - Create enums/status classes

2. **Application Layer** (`application/newentity/`)
   - Create request/response DTOs
   - Create MapStruct mapper interface
   - Create application service with `@Transactional`
   - Use domain service for validation

3. **Infrastructure Layer** (`infrastructure/persistence/newentity/`)
   - Create Spring Data JPA repository interface
   - Implement domain repository interface

4. **Presentation Layer** (`presentation/rest/newentity/`)
   - Create REST controller
   - Add validation annotations to DTOs

5. **Database Migration** (`src/main/resources/db/migration/`)
   - Create Flyway migration file: `V{n}__description.sql`
   - Must be compatible with both PostgreSQL and H2 for tests

6. **Tests**
   - Create unit test for domain logic
   - Create component test for REST endpoints

### Important Implementation Notes

- **Never expose entities in API** - always use DTOs
- **Use `@Transactional`** at application service layer, not domain
- **Soft delete pattern**: Call `entity.markAsDeleted()` instead of `repository.deleteById()`
- **Email validation**: Use `Email.of()` from domain layer - includes regex validation
- **Exception handling**: Use domain exceptions (`ResourceNotFoundException`, `BusinessConflictException`)
- **Logging**: Use `@Slf4j` with appropriate levels; trace ID is automatically included

## CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs on:
- Push to `main`, `develop`, `feature/*`, `hotfix/*`, `release/*`
- Pull requests to `main` or `develop`

### Pipeline Stages
1. **code-quality**: Checkstyle, SpotBugs
2. **build**: Compile and verify artifacts
3. **unit-tests**: Run test suite
4. **code-coverage**: Generate JaCoCo report
5. **package**: Create JAR with build info
6. **ci-summary**: Post results as PR comment

### Build Info
Git commit SHA is embedded in JAR via `git-commit-id-maven-plugin` and accessible via `/actuator/info`.

## Debugging & Monitoring

### Actuator Endpoints
- `GET /actuator/health` - Health check (shows git commit SHA)
- `GET /actuator/info` - Build and git information
- `GET /actuator/metrics` - Application metrics
- `GET /swagger-ui.html` - Interactive API documentation

### Logging
- Logs written to `logs/application.log`
- Trace ID automatically included in log format for request tracking
- Set `LOG_LEVEL` environment variable to adjust verbosity
- In dev: SQL statements logged at DEBUG level

### Common Issues

**Circular dependency warnings**: Usually between SecurityConfig and OAuth2 handlers. Resolved with `@Lazy` annotation.

**Flyway validation failures**: Check migration files are compatible with both PostgreSQL and H2 (test database).

**Token expiration during development**: Default JWT expiration is 86400000ms (24 hours). Adjust via `JWT_EXPIRATION_MS` env var.

## API Conventions

### Response Format
All endpoints return `ApiResponse<T>` wrapper with:
- `success`: boolean
- `message`: String
- `data`: T (actual response payload)
- `timestamp`: LocalDateTime

### Error Response Format
```json
{
  "status": 400,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-01-08T12:00:00",
  "path": "/api/v1/users",
  "errors": [
    { "field": "email", "message": "Invalid email format" }
  ]
}
```

### Key Endpoints
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - Login (returns JWT)
- `GET /api/v1/users/{id}` - Get user by ID (authenticated)
- `PUT /api/v1/users/{id}` - Update user profile
- `PUT /api/v1/users/{id}/password` - Change password
- `GET /oauth2/authorization/google` - Google OAuth2 login
