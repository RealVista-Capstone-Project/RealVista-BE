# Project Structure Visualization

## Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                            │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  REST Controllers                                            │   │
│  │  - UserController                                            │   │
│  │  - AuthenticationController                                  │   │
│  │                                                               │   │
│  │  Exception Handlers                                          │   │
│  │  - GlobalExceptionHandler                                    │   │
│  └─────────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────────┘
                                 │ HTTP Request/Response
                                 ↓
┌─────────────────────────────────────────────────────────────────────┐
│                       APPLICATION LAYER                              │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Application Services                                        │   │
│  │  - UserApplicationService                                    │   │
│  │                                                               │   │
│  │  DTOs (Request/Response)                                     │   │
│  │  - CreateUserRequest, UserResponse, etc.                     │   │
│  │                                                               │   │
│  │  Mappers (MapStruct)                                         │   │
│  │  - UserMapper                                                │   │
│  └─────────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────────┘
                                 │ Use Cases
                                 ↓
┌─────────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER                                │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Entities (Business Objects)                                 │   │
│  │  - User                                                      │   │
│  │                                                               │   │
│  │  Value Objects                                               │   │
│  │  - Email                                                     │   │
│  │                                                               │   │
│  │  Domain Services                                             │   │
│  │  - UserDomainService                                         │   │
│  │                                                               │   │
│  │  Repository Interfaces (Contracts)                           │   │
│  │  - UserRepository                                            │   │
│  │                                                               │   │
│  │  Domain Exceptions                                           │   │
│  │  - DomainException, ResourceNotFoundException               │   │
│  └─────────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────────┘
                                 │ Implements
                                 ↓
┌─────────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE LAYER                            │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Repository Implementations                                  │   │
│  │  - UserRepositoryImpl                                        │   │
│  │  - UserJpaRepository (Spring Data JPA)                       │   │
│  │                                                               │   │
│  │  Security                                                    │   │
│  │  - SecurityConfig, JwtService                                │   │
│  │  - CustomUserDetailsService                                  │   │
│  │                                                               │   │
│  │  Configuration                                               │   │
│  │  - AppConfig, JpaConfig, OpenApiConfig                       │   │
│  └─────────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────────┘
                                 │
                                 ↓
                        ┌────────────────┐
                        │   PostgreSQL   │
                        └────────────────┘
```

## Request Flow Example

```
1. Client Request
   │
   ↓
┌──────────────────────────────────────┐
│  POST /api/v1/users                  │
│  {                                    │
│    "email": "user@example.com",      │
│    "password": "Pass123"             │
│  }                                    │
└───────────────┬──────────────────────┘
                │
                ↓
┌──────────────────────────────────────┐
│  PRESENTATION                         │
│  UserController.createUser()          │
│  - Receives request                   │
│  - Validates with @Valid              │
└───────────────┬──────────────────────┘
                │
                ↓
┌──────────────────────────────────────┐
│  APPLICATION                          │
│  UserApplicationService.createUser()  │
│  - Validates business rules           │
│  - Builds domain entity               │
│  - Calls repository                   │
│  - Maps to DTO                        │
└───────────────┬──────────────────────┘
                │
                ↓
┌──────────────────────────────────────┐
│  DOMAIN                               │
│  User (Entity)                        │
│  - Contains business logic            │
│  - Validates domain rules             │
│  UserRepository (Interface)           │
│  - Defines contract                   │
└───────────────┬──────────────────────┘
                │
                ↓
┌──────────────────────────────────────┐
│  INFRASTRUCTURE                       │
│  UserRepositoryImpl                   │
│  - Implements repository              │
│  - Saves to database                  │
└───────────────┬──────────────────────┘
                │
                ↓
┌──────────────────────────────────────┐
│  DATABASE                             │
│  PostgreSQL                           │
│  - Persists data                      │
└───────────────┬──────────────────────┘
                │
                ↓
        (Response flows back)
                │
                ↓
┌──────────────────────────────────────┐
│  Response                             │
│  {                                    │
│    "success": true,                   │
│    "data": {                          │
│      "id": 1,                         │
│      "email": "user@example.com"      │
│    }                                   │
│  }                                     │
└──────────────────────────────────────┘
```

## Module Dependency Graph

```
┌────────────────┐
│   Presentation │
└────────┬───────┘
         │ depends on
         ↓
┌────────────────┐
│   Application  │
└────────┬───────┘
         │ depends on
         ↓
┌────────────────┐
│     Domain     │ ← Core (No dependencies)
└────────┬───────┘
         │ implemented by
         ↓
┌────────────────┐
│ Infrastructure │
└────────────────┘
```

## Package Structure Tree

```
com.sep.realvista
│
├── domain
│   ├── common
│   │   ├── entity
│   │   │   └── BaseEntity.java
│   │   ├── exception
│   │   │   ├── DomainException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── BusinessConflictException.java
│   │   └── value
│   │       └── Email.java
│   │
│   └── user
│       ├── User.java                    (Entity)
│       ├── UserRepository.java          (Interface)
│       ├── UserDomainService.java       (Domain Service)
│       ├── UserRole.java                (Enum)
│       └── UserStatus.java              (Enum)
│
├── application
│   ├── common
│   │   └── dto
│   │       ├── ApiResponse.java
│   │       ├── ErrorResponse.java
│   │       └── PageResponse.java
│   │
│   └── user
│       ├── dto
│       │   ├── UserResponse.java
│       │   ├── CreateUserRequest.java
│       │   ├── UpdateUserRequest.java
│       │   └── ChangePasswordRequest.java
│       ├── mapper
│       │   └── UserMapper.java
│       └── service
│           └── UserApplicationService.java
│
├── infrastructure
│   ├── config
│   │   ├── AppConfig.java
│   │   ├── JpaConfig.java
│   │   ├── OpenApiConfig.java
│   │   └── security
│   │       ├── SecurityConfig.java
│   │       ├── JwtService.java
│   │       └── JwtAuthenticationFilter.java
│   │
│   ├── persistence
│   │   └── user
│   │       ├── UserJpaRepository.java
│   │       └── UserRepositoryImpl.java
│   │
│   └── security
│       └── CustomUserDetailsService.java
│
└── presentation
    ├── rest
    │   ├── auth
    │   │   ├── AuthenticationController.java
    │   │   ├── LoginRequest.java
    │   │   └── AuthenticationResponse.java
    │   │
    │   └── user
    │       └── UserController.java
    │
    └── exception
        └── GlobalExceptionHandler.java
```

## Technology Stack Diagram

```
┌─────────────────────────────────────────┐
│         Spring Boot 3.5.9               │
├─────────────────────────────────────────┤
│  Web Layer                               │
│  ├── Spring MVC                          │
│  ├── Spring Security + JWT               │
│  └── OpenAPI/Swagger                     │
├─────────────────────────────────────────┤
│  Business Layer                          │
│  ├── Spring Core (DI)                    │
│  ├── MapStruct (Mapping)                 │
│  └── Lombok (Boilerplate)                │
├─────────────────────────────────────────┤
│  Data Layer                              │
│  ├── Spring Data JPA                     │
│  ├── Hibernate                           │
│  ├── Flyway (Migrations)                 │
│  └── PostgreSQL Driver                   │
├─────────────────────────────────────────┤
│  Infrastructure                          │
│  ├── Spring Cache                        │
│  ├── Spring Actuator                     │
│  └── Logback                             │
├─────────────────────────────────────────┤
│  Testing                                 │
│  ├── JUnit 5                             │
│  ├── Mockito                             │
│  └── Testcontainers                      │
├─────────────────────────────────────────┤
│  Quality                                 │
│  ├── Checkstyle                          │
│  ├── SpotBugs                            │
│  └── JaCoCo                              │
└─────────────────────────────────────────┘
```

## Database Schema

```
┌────────────────────────────────────────┐
│              users                      │
├────────────────────────────────────────┤
│ id (PK)          BIGSERIAL              │
│ value            VARCHAR(255) UNIQUE    │ ← Email
│ password_hash    VARCHAR(255)           │
│ first_name       VARCHAR(100)           │
│ last_name        VARCHAR(100)           │
│ status           VARCHAR(20)            │ ← PENDING, ACTIVE, etc.
│ role             VARCHAR(20)            │ ← USER, ADMIN, MODERATOR
│ avatar_url       VARCHAR(500)           │
│ created_at       TIMESTAMP              │
│ updated_at       TIMESTAMP              │
│ deleted          BOOLEAN                │
└────────────────────────────────────────┘

Indexes:
- idx_user_email (value)
- idx_user_status (status)
- idx_user_deleted (deleted)
```

## Security Flow

```
┌──────────┐
│  Client  │
└────┬─────┘
     │
     │ 1. POST /api/v1/auth/login
     │    { email, password }
     ↓
┌─────────────────────┐
│ AuthController      │
└────┬────────────────┘
     │
     │ 2. Authenticate
     ↓
┌─────────────────────┐
│ AuthenticationMgr   │
└────┬────────────────┘
     │
     │ 3. Load user
     ↓
┌─────────────────────┐
│ UserDetailsService  │
└────┬────────────────┘
     │
     │ 4. Verify password
     ↓
┌─────────────────────┐
│ PasswordEncoder     │
└────┬────────────────┘
     │
     │ 5. Generate JWT
     ↓
┌─────────────────────┐
│ JwtService          │
└────┬────────────────┘
     │
     │ 6. Return token
     ↓
┌──────────┐
│  Client  │ → Stores token
└────┬─────┘
     │
     │ 7. Subsequent requests
     │    Authorization: Bearer <token>
     ↓
┌─────────────────────┐
│ JwtAuthFilter       │ → Validates token
└────┬────────────────┘
     │
     │ 8. Set Authentication
     ↓
┌─────────────────────┐
│ SecurityContext     │
└─────────────────────┘
```

## Error Handling Flow

```
Request
   │
   ↓
Controller
   │
   ├─ Valid? ──No──→ MethodArgumentNotValidException
   │                           │
   │                           ↓
   │                 GlobalExceptionHandler
   │                           │
   │                           ↓
   │                 ValidationErrorResponse
   │
   Yes
   │
   ↓
Service
   │
   ├─ Business Valid? ──No──→ DomainException
   │                                │
   │                                ↓
   │                    GlobalExceptionHandler
   │                                │
   │                                ↓
   │                      ErrorResponse
   │
   Yes
   │
   ↓
Repository
   │
   ├─ Found? ──No──→ ResourceNotFoundException
   │                           │
   │                           ↓
   │                 GlobalExceptionHandler
   │                           │
   │                           ↓
   │                    ErrorResponse (404)
   │
   Yes
   │
   ↓
Success Response
```

---

For more details, refer to:
- README.md
- ARCHITECTURE.md
- QUICKSTART.md
- CONTRIBUTING.md

