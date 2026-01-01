# Development Guidelines - RealVista Backend

## Table of Contents
1. [Code Style](#code-style)
2. [Architecture Principles](#architecture-principles)
3. [Naming Conventions](#naming-conventions)
4. [Best Practices](#best-practices)
5. [Git Workflow](#git-workflow)
6. [Testing Guidelines](#testing-guidelines)

---

## Code Style

### Java Conventions
- Use **4 spaces** for indentation (no tabs)
- Max line length: **120 characters**
- Use **camelCase** for variables and methods
- Use **PascalCase** for classes
- Use **UPPER_SNAKE_CASE** for constants

### Package Structure
```
com.sep.realvista
├── domain.{module}          # Domain entities, repositories, services
├── application.{module}     # DTOs, mappers, application services
├── infrastructure.{module}  # Repository implementations, configs
└── presentation.rest.{module} # REST controllers
```

### Import Organization
1. Java standard libraries
2. Third-party libraries
3. Spring Framework
4. Project imports

---

## Architecture Principles

### 1. Separation of Concerns
- Each layer has a single responsibility
- No business logic in controllers
- No infrastructure code in domain layer

### 2. Dependency Rule
```
Domain (no dependencies)
   ↑
Application (depends on Domain)
   ↑
Infrastructure & Presentation (depends on Application & Domain)
```

### 3. Don't Expose Entities
❌ **Wrong**:
```java
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id);
}
```

✅ **Correct**:
```java
@GetMapping("/{id}")
public UserResponse getUser(@PathVariable Long id) {
    return userService.getUserById(id);
}
```

---

## Naming Conventions

### Classes
- **Entity**: `User`, `Product`, `Order`
- **DTO**: `UserResponse`, `CreateUserRequest`, `UpdateUserRequest`
- **Service**: `UserApplicationService`, `UserDomainService`
- **Repository**: `UserRepository`, `UserRepositoryImpl`
- **Controller**: `UserController`, `AuthenticationController`
- **Mapper**: `UserMapper`, `ProductMapper`

### Methods
- **Get**: `getUserById()`, `findUserByEmail()`
- **Create**: `createUser()`, `registerUser()`
- **Update**: `updateUser()`, `changePassword()`
- **Delete**: `deleteUser()`, `removeProduct()`
- **Validate**: `validateEmail()`, `checkPermission()`

### API Endpoints
- Use **kebab-case**
- Use **plural nouns**
- Version your API

```
✅ /api/v1/users
✅ /api/v1/users/{id}
✅ /api/v1/users/{id}/orders
✅ /api/v1/users/{id}/password-reset

❌ /api/v1/user
❌ /api/v1/User
❌ /api/v1/get-user
```

---

## Best Practices

### 1. Use DTOs, Not Entities
```java
// ✅ Good
public UserResponse createUser(CreateUserRequest request) {
    User user = userMapper.toEntity(request);
    User saved = userRepository.save(user);
    return userMapper.toResponse(saved);
}

// ❌ Bad
public User createUser(User user) {
    return userRepository.save(user);
}
```

### 2. Constructor Injection
```java
// ✅ Good - with Lombok
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
}

// ❌ Bad - field injection
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### 3. Transaction Management
```java
// ✅ Good
@Service
@Transactional
public class UserService {
    public void updateUser(Long id, UpdateRequest request) {
        User user = getUserOrThrow(id);
        user.update(request);
        userRepository.save(user);
    }
}
```

### 4. Exception Handling
```java
// ✅ Good - domain exception
public User getUserOrThrow(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
}

// ❌ Bad - generic exception
public User getUserOrThrow(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("User not found"));
}
```

### 5. Input Validation
```java
// ✅ Good
public class CreateUserRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}

// Controller
@PostMapping
public ResponseEntity<UserResponse> create(
        @Valid @RequestBody CreateUserRequest request) {
    // ...
}
```

### 6. Logging
```java
// ✅ Good
log.info("Creating user - traceId: {}, email: {}", traceId, email);
log.error("Failed to create user - traceId: {}, error: {}", traceId, e.getMessage());

// ❌ Bad
System.out.println("Creating user");  // Never use System.out
log.info("User: " + user.toString()); // Use parameterized logging
log.error("Error: " + e.getMessage()); // Log exception object
```

### 7. Avoid Magic Numbers
```java
// ✅ Good
private static final int MAX_LOGIN_ATTEMPTS = 5;
private static final long TOKEN_EXPIRATION_MS = 86400000L;

if (attempts > MAX_LOGIN_ATTEMPTS) {
    lockAccount();
}

// ❌ Bad
if (attempts > 5) {
    lockAccount();
}
```

---

## Git Workflow

### Branch Naming
```
feature/PROJ-123-user-authentication
bugfix/PROJ-456-fix-login-error
hotfix/PROJ-789-security-patch
release/v1.0.0
```

### Commit Messages
Follow Conventional Commits:
```
feat: add user registration endpoint
fix: resolve null pointer in user service
docs: update API documentation
test: add integration tests for auth
refactor: simplify user mapper logic
chore: update dependencies
```

### Pull Request Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added
- [ ] Integration tests added
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings
```

---

## Testing Guidelines

### Unit Test Structure
```java
@Test
void methodName_condition_expectedBehavior() {
    // Given (Arrange)
    User user = createTestUser();
    
    // When (Act)
    UserResponse response = userService.createUser(request);
    
    // Then (Assert)
    assertThat(response.getEmail()).isEqualTo("test@example.com");
}
```

### Test Coverage Requirements
- **Minimum**: 70% line coverage
- **Target**: 80% line coverage
- Focus on business logic

### Integration Tests
```java
@SpringBootTest
@Testcontainers
class UserControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15");
    
    @Test
    void createUser_withValidData_shouldReturn201() {
        // Test implementation
    }
}
```

### Test Data Builders
```java
public class UserTestBuilder {
    public static User.UserBuilder defaultUser() {
        return User.builder()
            .email(Email.of("test@example.com"))
            .firstName("John")
            .lastName("Doe")
            .status(UserStatus.ACTIVE);
    }
}

// Usage
User user = UserTestBuilder.defaultUser()
    .email(Email.of("custom@example.com"))
    .build();
```

---

## Code Review Checklist

### Functionality
- [ ] Does the code work as intended?
- [ ] Are edge cases handled?
- [ ] Is error handling appropriate?

### Architecture
- [ ] Follows clean architecture principles?
- [ ] Proper layer separation?
- [ ] Dependencies point inward?

### Code Quality
- [ ] Follows coding standards?
- [ ] No code duplication?
- [ ] Meaningful variable names?
- [ ] Comments for complex logic?

### Testing
- [ ] Unit tests present?
- [ ] Integration tests present?
- [ ] Tests are meaningful?
- [ ] Coverage meets requirements?

### Security
- [ ] Input validation present?
- [ ] No SQL injection vulnerabilities?
- [ ] Proper authorization checks?
- [ ] No sensitive data in logs?

### Performance
- [ ] No N+1 query problems?
- [ ] Proper use of indexes?
- [ ] Pagination for large datasets?
- [ ] Caching implemented where needed?

---

## Common Pitfalls to Avoid

### 1. Exposing Internal Details
❌ Don't expose database entities in API
❌ Don't leak implementation details in errors

### 2. Improper Exception Handling
❌ Don't catch generic `Exception`
❌ Don't swallow exceptions
❌ Don't log and rethrow

### 3. Poor Performance
❌ Don't use `findAll()` without pagination
❌ Don't make database calls in loops
❌ Don't load unnecessary associations

### 4. Security Issues
❌ Don't log sensitive data (passwords, tokens)
❌ Don't trust user input
❌ Don't hardcode secrets

---

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://martinfowler.com/tags/domain%20driven%20design.html)
- [Effective Java](https://www.oreilly.com/library/view/effective-java/9780134686097/)

---

**Questions?** Ask the team lead or refer to existing code examples.

