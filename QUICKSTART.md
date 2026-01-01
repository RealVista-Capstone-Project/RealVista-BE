# Quick Start Guide - RealVista Backend

## Prerequisites Checklist
- [ ] Java 21 installed (`java -version`)
- [ ] Maven 3.8+ installed (`mvn -version`)
- [ ] Docker & Docker Compose installed (`docker --version`)
- [ ] Git installed

---

## Step-by-Step Setup

### 1. Start Database
```bash
# Start PostgreSQL with Docker Compose
docker-compose up -d

# Verify database is running
docker ps

# Check logs (optional)
docker-compose logs postgres
```

Access PgAdmin: http://localhost:5050
- Email: admin@realvista.com
- Password: admin

### 2. Build Project
```bash
# Clean and install dependencies
mvn clean install

# Skip tests for faster build (optional)
mvn clean install -DskipTests
```

### 3. Run Application
```bash
# Run with Maven
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Verify Application
```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

---

## API Testing

### Using cURL

#### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@realvista.com",
    "password": "Password123"
  }'
```

**Save the token** from the response!

#### 3. Get User by ID
```bash
# Replace {TOKEN} with your JWT token
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer {TOKEN}"
```

#### 4. Update User Profile
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith"
  }'
```

---

## Using Swagger UI

1. Open browser: http://localhost:8080/swagger-ui.html
2. Click **"Authorize"** button
3. Enter: `Bearer {YOUR_TOKEN}`
4. Try endpoints interactively

---

## Default Test Accounts

| Email | Password | Role |
|-------|----------|------|
| admin@realvista.com | Password123 | ADMIN |
| user@realvista.com | Password123 | USER |
| pending@realvista.com | Password123 | USER (Pending) |

---

## Common Commands

### Maven Commands
```bash
# Compile
mvn compile

# Run tests
mvn test

# Package (create JAR)
mvn package

# Clean build
mvn clean install

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker Commands
```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f

# Restart services
docker-compose restart

# Remove volumes (WARNING: deletes data)
docker-compose down -v
```

### Database Commands
```bash
# Connect to PostgreSQL
docker exec -it realvista-postgres psql -U postgres -d realvista_db

# Inside psql:
\dt                    # List tables
\d users              # Describe users table
SELECT * FROM users;  # Query users
\q                    # Quit
```

---

## Code Quality Checks

### Run All Checks
```bash
mvn clean verify
```

### Individual Checks
```bash
# Checkstyle
mvn checkstyle:check

# SpotBugs
mvn spotbugs:check

# JaCoCo Coverage
mvn jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

---

## Troubleshooting

### Port Already in Use
```bash
# Check what's using port 8080
lsof -i :8080

# Kill process (Mac/Linux)
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

### Database Connection Error
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Restart database
docker-compose restart postgres

# Check database logs
docker-compose logs postgres
```

### Compilation Errors
```bash
# Clean and rebuild
mvn clean compile

# Update dependencies
mvn clean install -U
```

### MapStruct Generation Issues
```bash
# Clean generated sources
mvn clean

# Rebuild with annotation processing
mvn clean compile -Dmapstruct.verbose=true
```

---

## IDE Setup

### IntelliJ IDEA
1. Open project (`File > Open`)
2. Wait for Maven import
3. Enable annotation processing:
   - `Settings > Build > Compiler > Annotation Processors`
   - Check "Enable annotation processing"
4. Install Lombok plugin
5. Run `RealvistaApplication.java`

### VS Code
1. Install extensions:
   - Java Extension Pack
   - Spring Boot Extension Pack
   - Lombok Annotations Support
2. Open project folder
3. Run with Spring Boot Dashboard

### Eclipse
1. Import Maven project
2. Install Lombok:
   - Download lombok.jar
   - Run `java -jar lombok.jar`
3. Enable annotation processing

---

## Next Steps

✅ **You're all set!** Now you can:

1. Explore the codebase structure
2. Add new features following the architecture
3. Write tests for your code
4. Check API documentation in Swagger
5. Review `ARCHITECTURE.md` for design patterns

---

## Useful Links

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **PgAdmin**: http://localhost:5050
- **Logs**: `logs/application.log`

---

## Getting Help

- Check `README.md` for detailed documentation
- Review `ARCHITECTURE.md` for architecture details
- Check logs: `logs/application.log`
- Debug with IDE breakpoints

---

**Happy Coding! 🚀**

