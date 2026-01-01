# RealVista Backend - Architecture Documentation

## Clean Architecture Overview

This project implements Clean Architecture with Domain-Driven Design (DDD) principles, ensuring:
- **Independence**: Business logic independent of frameworks and external systems
- **Testability**: Easy to test without UI, database, or external services
- **Flexibility**: Easy to change databases, frameworks, or UI
- **Maintainability**: Clear separation of concerns

## Layer Dependencies

```
Presentation → Application → Domain
         ↓
Infrastructure
```

**Rule**: Dependencies point inward. Domain has NO dependencies on outer layers.

---

## 1. Domain Layer

**Purpose**: Contains core business logic and rules.

**Components**:
- **Entities**: Business objects with identity (e.g., `User`)
- **Value Objects**: Immutable objects without identity (e.g., `Email`)
- **Domain Services**: Business logic that doesn't fit in entities
- **Repository Interfaces**: Contracts for data access (no implementation)
- **Domain Exceptions**: Business rule violations

**Example**:
```java
// Entity
@Entity
public class User extends BaseEntity {
    private Email email;
    private String passwordHash;
    
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        this.status = UserStatus.ACTIVE;
    }
}

// Value Object
@Embeddable
public class Email {
    private String value;
    
    public static Email of(String email) {
        // Validation logic
        return new Email(email);
    }
}

// Repository Interface
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
}
```

---

## 2. Application Layer

**Purpose**: Orchestrates business logic and use cases.

**Components**:
- **DTOs**: Data Transfer Objects for API communication
- **Mappers**: Convert between entities and DTOs (MapStruct)
- **Application Services**: Coordinate domain objects and repositories
- **Use Cases**: Specific business operations

**Example**:
```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Validate
        userDomainService.validateUniqueEmail(request.getEmail());
        
        // 2. Build domain entity
        User user = User.builder()
            .email(Email.of(request.getEmail()))
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .build();
        
        // 3. Save
        User savedUser = userRepository.save(user);
        
        // 4. Map to DTO
        return userMapper.toResponse(savedUser);
    }
}
```

---

## 3. Infrastructure Layer

**Purpose**: Implements technical capabilities and external systems.

**Components**:
- **Repository Implementations**: JPA/JDBC implementations
- **Configuration**: Spring configuration classes
- **Security**: JWT, authentication, authorization
- **External Services**: Email, cloud storage, third-party APIs

**Example**:
```java
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository jpaRepository;
    
    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }
}

interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(Email email);
}
```

---

## 4. Presentation Layer

**Purpose**: Handles HTTP requests and responses.

**Components**:
- **Controllers**: REST endpoints
- **Exception Handlers**: Global error handling
- **Request/Response DTOs**: API contracts

**Example**:
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", user));
    }
}
```

---

## Design Patterns Used

### 1. Repository Pattern
- Domain defines interface
- Infrastructure implements it
- Decouples business logic from data access

### 2. Service Pattern
- Application Service: Orchestrates use cases
- Domain Service: Contains business logic

### 3. DTO Pattern
- Never expose entities in API
- Use DTOs for external communication

### 4. Builder Pattern
- Constructs complex objects
- Used with Lombok `@Builder`

### 5. Strategy Pattern
- Used in authentication providers

---

## API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-01-01T12:00:00"
}
```

### Error Response
```json
{
  "status": 400,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-01-01T12:00:00",
  "path": "/api/v1/users",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format"
    }
  ]
}
```

### Paginated Response
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

## Security Architecture

### JWT Authentication Flow

1. User logs in with email/password
2. Server validates credentials
3. Server generates JWT token
4. Client includes token in `Authorization: Bearer <token>`
5. Server validates token on each request

### Role-Based Access Control (RBAC)

```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { }

@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
public void adminOrOwnerMethod(Long userId) { }
```

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    value VARCHAR(255) NOT NULL UNIQUE,  -- email
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
```

---

## Testing Strategy

### Unit Tests
- Test business logic in isolation
- Mock dependencies
- Fast execution

### Integration Tests
- Test entire request flow
- Use Testcontainers for database
- Test API endpoints

### Example:
```java
@SpringBootTest
@Testcontainers
class UserControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void createUser_withValidData_shouldReturnCreated() {
        // Test implementation
    }
}
```

---

## Logging Best Practices

### Include Trace ID
```java
String traceId = UUID.randomUUID().toString();
MDC.put("traceId", traceId);
log.info("Processing request - traceId: {}", traceId);
```

### Log Levels
- **ERROR**: System errors, exceptions
- **WARN**: Potential issues
- **INFO**: Important business events
- **DEBUG**: Detailed debugging info
- **TRACE**: Very detailed debugging

---

## Performance Optimization

### 1. Caching
```java
@Cacheable(value = "users", key = "#userId")
public UserResponse getUserById(Long userId) { }

@CacheEvict(value = "users", key = "#userId")
public void updateUser(Long userId) { }
```

### 2. Database Indexing
- Email column (for lookups)
- Status column (for filtering)
- Composite indexes for common queries

### 3. Query Optimization
- Use JPQL with JOIN FETCH
- Paginate large result sets
- Avoid N+1 queries

---

## Migration Guide

### Adding a New Domain Entity

1. **Create Entity** (`domain/entity`)
```java
@Entity
public class Product extends BaseEntity {
    private String name;
    private BigDecimal price;
}
```

2. **Create Repository Interface** (`domain/entity`)
```java
public interface ProductRepository {
    Product save(Product product);
}
```

3. **Create DTOs** (`application/entity/dto`)
```java
public class ProductResponse { }
public class CreateProductRequest { }
```

4. **Create Mapper** (`application/entity/mapper`)
```java
@Mapper(componentModel = "spring")
public interface ProductMapper { }
```

5. **Create Application Service** (`application/entity/service`)
```java
@Service
public class ProductApplicationService { }
```

6. **Implement Repository** (`infrastructure/persistence/entity`)
```java
@Repository
public class ProductRepositoryImpl implements ProductRepository { }
```

7. **Create Controller** (`presentation/rest/entity`)
```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController { }
```

8. **Create Migration** (`resources/db/migration`)
```sql
-- V3__Create_products_table.sql
```

---

## Monitoring and Health Checks

### Actuator Endpoints
- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

---

## Future Enhancements

- [ ] Add Redis for distributed caching
- [ ] Implement event-driven architecture (Kafka/RabbitMQ)
- [ ] Add API rate limiting
- [ ] Implement audit logging
- [ ] Add file upload service
- [ ] Multi-tenant support
- [ ] GraphQL API support

---

For more details, refer to the source code and inline documentation.

