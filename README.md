# RealVista Backend - Spring Boot Clean Architecture

A production-ready Spring Boot backend application implementing Clean Architecture and Domain-Driven Design (DDD) principles.

## 🏗️ Architecture Overview

This project follows Clean Architecture with DDD, organized into four main layers:

```
src/main/java/com/sep/realvista/
├── domain/              # Domain Layer (Business Logic)
├── application/         # Application Layer (Use Cases)
├── infrastructure/      # Infrastructure Layer (External Systems)
└── presentation/        # Presentation Layer (REST Controllers)
```

### Layer Responsibilities

- **Domain Layer**: Contains business entities, value objects, domain services, and repository interfaces
- **Application Layer**: Orchestrates business logic, DTOs, mappers, and application services
- **Infrastructure Layer**: Implements repository interfaces, external services, security, and configuration
- **Presentation Layer**: REST controllers, request/response handling, and exception handlers

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+ (optional if using Docker)

### Quick Start

1. **Clone the repository**
   ```bash
   cd /path/to/realvista
   ```

2. **Start PostgreSQL with Docker**
   ```bash
   docker-compose up -d
   ```

3. **Configure environment variables** (optional)
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the application**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - PgAdmin: http://localhost:5050 (admin@realvista.com / admin)

## 📋 Default Test Users

| Email | Password | Role |
|-------|----------|------|
| admin@realvista.com | Password123 | ADMIN |
| user@realvista.com | Password123 | USER |
| pending@realvista.com | Password123 | USER (Pending) |

## 🔧 Configuration

### Database Configuration

Edit `application.properties` or set environment variables:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=realvista_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### JWT Configuration

```properties
JWT_SECRET=your-secret-key
JWT_EXPIRATION_MS=86400000
```

### Profiles

- **dev**: Development profile with debug logging
- **prod**: Production profile with optimized settings

Run with specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🧪 Testing

### Run all tests
```bash
mvn test
```

### Run integration tests
```bash
mvn verify
```

### Code coverage report
```bash
mvn jacoco:report
```
View report at: `target/site/jacoco/index.html`

## 🔍 Code Quality

### Run Checkstyle
```bash
mvn checkstyle:check
```

### Run SpotBugs
```bash
mvn spotbugs:check
```

### Run all quality checks
```bash
mvn clean verify
```

## 📚 API Documentation

Once the application is running, access:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Sample API Endpoints

#### Authentication
```bash
# Register
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}

# Login
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@realvista.com",
  "password": "Password123"
}
```

#### User Management
```bash
# Get user by ID
GET /api/v1/users/{id}
Authorization: Bearer {token}

# Update user profile
PUT /api/v1/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "avatarUrl": "https://example.com/avatar.jpg"
}

# Change password
PUT /api/v1/users/{id}/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "Password123",
  "newPassword": "NewSecurePass123"
}
```

## 🗂️ Project Structure

```
realvista/
├── src/
│   ├── main/
│   │   ├── java/com/sep/realvista/
│   │   │   ├── domain/
│   │   │   │   ├── common/          # Shared domain components
│   │   │   │   └── user/            # User domain
│   │   │   ├── application/
│   │   │   │   ├── common/          # Common DTOs
│   │   │   │   └── user/            # User application services
│   │   │   ├── infrastructure/
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   ├── persistence/     # JPA repositories
│   │   │   │   └── security/        # Security implementation
│   │   │   └── presentation/
│   │   │       ├── rest/            # REST controllers
│   │   │       └── exception/       # Exception handlers
│   │   └── resources/
│   │       ├── db/migration/        # Flyway migrations
│   │       ├── application.properties
│   │       └── logback-spring.xml
│   └── test/
├── checkstyle.xml
├── compose.yaml
├── pom.xml
└── README.md
```

## 🔐 Security

- **Authentication**: JWT-based authentication
- **Authorization**: Role-based access control (RBAC)
- **Password**: BCrypt hashing with salt
- **Input Validation**: Jakarta Bean Validation
- **SQL Injection**: Prevented with JPA/JPQL parameterized queries

## 📝 Development Guidelines

### Adding a New Feature

1. **Domain Layer**: Create entity, repository interface, domain service
2. **Application Layer**: Create DTOs, mapper, application service
3. **Infrastructure Layer**: Implement repository
4. **Presentation Layer**: Create controller with endpoints
5. **Database**: Add Flyway migration if needed
6. **Tests**: Write unit and integration tests

### Code Standards

- Follow checkstyle rules (see `checkstyle.xml`)
- Write meaningful commit messages
- Add JavaDoc for public methods
- Maintain test coverage > 70%
- Use constructor injection with Lombok's `@RequiredArgsConstructor`

### Branching Strategy

```
feature/PROJ-123-description
bugfix/PROJ-456-description
hotfix/PROJ-789-description
release/v1.0.0
```

## 🐳 Docker

### Build Docker image
```bash
docker build -t realvista-backend:latest .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

## 📊 Monitoring

- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Logs**: `logs/application.log`

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Run tests and quality checks
4. Submit a pull request

## 📄 License

This project is licensed under the Apache License 2.0.

## 👥 Team

RealVista Development Team - Spring 2026

## 📮 Support

For issues or questions, contact: contact@realvista.com

