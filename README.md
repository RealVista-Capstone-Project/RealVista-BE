# RealVista Backend

![CI Pipeline](https://github.com/YOUR_USERNAME/realvista/workflows/CI%20Pipeline/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

A production-ready Spring Boot backend application implementing Clean Architecture and Domain-Driven Design (DDD) principles with comprehensive CI/CD pipeline.

## 🚀 Quick Links

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Git Info**: http://localhost:8080/actuator/info
- **Metrics**: http://localhost:8080/actuator/metrics

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [CI/CD Pipeline](#cicd-pipeline)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Documentation](#documentation)

## ✨ Features

### Core Features

- ✅ **Clean Architecture** with DDD principles
- ✅ **JWT Authentication** with role-based access control
- ✅ **RESTful API** with OpenAPI/Swagger documentation
- ✅ **Database Migration** with Flyway
- ✅ **Caching** support with Spring Cache
- ✅ **Logging** with trace ID support
- ✅ **Actuator** endpoints for monitoring

### Code Quality

- ✅ **Checkstyle** for code style enforcement
- ✅ **SpotBugs** for static analysis
- ✅ **JaCoCo** for code coverage (70% minimum)
- ✅ **MapStruct** for DTO mapping
- ✅ **Lombok** for boilerplate reduction

### DevOps

- ✅ **CI Pipeline** with GitHub Actions
- ✅ **Automated Testing** with PostgreSQL
- ✅ **Security Scanning** with Trivy
- ✅ **Git SHA Tracking** in health endpoint
- ✅ **Build Info** in actuator

## 🏗️ Architecture

```
src/main/java/com/sep/realvista/
├── domain/              # Business Logic (Core)
│   ├── user/
│   └── common/
├── application/         # Use Cases & DTOs
│   └── user/
├── infrastructure/      # Technical Implementation
│   ├── config/
│   ├── persistence/
│   └── security/
└── presentation/        # REST Controllers
    └── rest/
```

**Layer Dependencies**: Presentation → Application → Domain ← Infrastructure

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Installation

1. **Clone repository**

   ```bash
   git clone https://github.com/YOUR_USERNAME/realvista.git
   cd realvista
   ```

2. **Start database**

   ```bash
   docker-compose up -d
   ```

3. **Build application**

   ```bash
   mvn clean install
   ```

4. **Run application**

   ```bash
   mvn spring-boot:run
   ```

5. **Verify**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Docker Setup

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## 🔄 CI/CD Pipeline

### Automated Checks

Our GitHub Actions pipeline ensures:

✅ **Code Quality**

- Checkstyle validation
- SpotBugs analysis
- Code formatting

✅ **Build**

- Compilation success
- Artifact generation
- Build info extraction

✅ **Testing**

- Unit tests execution
- Integration tests with PostgreSQL
- 70% code coverage minimum

✅ **Security**

- Dependency vulnerability scan
- Trivy security analysis

✅ **Package**

- JAR creation
- Git SHA embedding
- Metadata extraction

### Pipeline Status

View pipeline status: [Actions Tab](https://github.com/YOUR_USERNAME/realvista/actions)

### Branch Strategy

```
main (protected) ← Pull Requests only
  ↑
develop
  ↑
feature/* | bugfix/* | hotfix/*
```

**Protection Rules**:

- Require PR approval
- All CI checks must pass
- Up-to-date branches required

## 💻 Development

### Run Locally

```bash
# Development mode with hot reload
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# With custom port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Database Migration (Flyway)

```bash
# Clean database (CAUTION: drops all tables)
./mvnw flyway:clean -Dflyway.cleanDisabled=false

# Run migrations
./mvnw flyway:migrate

# Clean and migrate (fresh start)
./mvnw flyway:clean -Dflyway.cleanDisabled=false && ./mvnw flyway:migrate

# Check migration info
./mvnw flyway:info
```

> ⚠️ **Warning**: `flyway:clean` will delete ALL data. Use only in development!

### Code Quality

```bash
# Run all checks
mvn clean verify

# Individual checks
mvn checkstyle:check
mvn spotbugs:check
mvn jacoco:report
```

### Test Accounts

| Email               | Password    | Role  |
| ------------------- | ----------- | ----- |
| admin@realvista.com | Password123 | ADMIN |
| user@realvista.com  | Password123 | USER  |

## 🧪 Testing

### Unit Tests

```bash
# Run tests
mvn test

# With coverage
mvn test jacoco:report

# View coverage
open target/site/jacoco/index.html
```

### Integration Tests

```bash
# Run with Testcontainers
mvn verify

# Specific test
mvn test -Dtest=UserControllerTest
```

### API Testing

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@realvista.com","password":"Password123"}'

# Use token
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 📦 Deployment

### Build for Production

```bash
# Create JAR
mvn clean package -Pprod

# Run JAR
java -jar target/realvista-0.0.1-SNAPSHOT.jar
```

### Environment Variables

```bash
export DB_HOST=your-db-host
export DB_PORT=5432
export DB_NAME=realvista_db
export DB_USERNAME=postgres
export DB_PASSWORD=secure-password
export JWT_SECRET=your-secret-key
export SPRING_PROFILES_ACTIVE=prod
```

### Health Checks

```bash
# Basic health
curl http://localhost:8080/actuator/health

# Detailed (requires auth)
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/actuator/health

# Git information
curl http://localhost:8080/actuator/info
```

## 📚 Documentation

- [Architecture Guide](ARCHITECTURE.md) - Detailed architecture documentation
- [Quick Start Guide](QUICKSTART.md) - Step-by-step setup
- [Contributing Guidelines](CONTRIBUTING.md) - Development standards
- [CI/CD Pipeline](CI_PIPELINE_GUIDE.md) - Pipeline documentation
- [Configuration Changes](CONFIGURATION_CHANGES.md) - Recent updates
- [Project Summary](PROJECT_SUMMARY.md) - Complete overview
- [Branch Protection](.github/BRANCH_PROTECTION.md) - Branch rules

### API Documentation

Access Swagger UI: http://localhost:8080/swagger-ui.html

### Key Endpoints

```
Authentication:
POST   /api/v1/auth/register
POST   /api/v1/auth/login

User Management:
GET    /api/v1/users/{id}
POST   /api/v1/users
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}
PUT    /api/v1/users/{id}/password

Actuator:
GET    /actuator/health
GET    /actuator/info
GET    /actuator/metrics
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'feat: add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## 📊 Monitoring

### Metrics

Access metrics at: http://localhost:8080/actuator/metrics

Available metrics:

- JVM memory usage
- HTTP request counts
- Database connection pool
- Cache statistics

### Logging

Logs are available at:

- `logs/application.log` - All logs
- `logs/error.log` - Error logs only

Log format includes trace ID for request tracking.

## 🔒 Security

- **Authentication**: JWT-based
- **Authorization**: Role-based (RBAC)
- **Password**: BCrypt hashing
- **Input Validation**: Jakarta Bean Validation
- **SQL Injection**: Prevented with JPA
- **Security Scan**: Automated with Trivy

## 📜 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 👥 Team

RealVista Development Team - Spring 2026

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/YOUR_USERNAME/realvista/issues)
- **Email**: contact@realvista.com
- **Documentation**: See `/docs` folder

## 🎯 Roadmap

### Current (v0.0.1)

- ✅ Clean Architecture setup
- ✅ JWT Authentication
- ✅ User Management
- ✅ CI Pipeline

### Coming Soon (v0.1.0)

- [ ] CD Pipeline (when cloud ready)
- [ ] Email verification
- [ ] Refresh tokens
- [ ] File upload service
- [ ] Audit logging

### Future (v1.0.0)

- [ ] Redis caching
- [ ] Event-driven architecture
- [ ] API rate limiting
- [ ] Multi-tenant support
- [ ] GraphQL API

---

**Built with ❤️ using Spring Boot and Clean Architecture**

**Status**: ✅ Production Ready | 🔄 CI Pipeline Active | 🧪 Tests Passing
