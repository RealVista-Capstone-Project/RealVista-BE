# GitHub Actions CI/CD Pipeline Documentation

## 🎯 Overview

This project uses GitHub Actions for Continuous Integration (CI). The pipeline ensures code quality, builds successfully, runs all tests, and packages the application.

## 📋 Pipeline Workflows

### 1. Main CI Pipeline (`ci.yml`)

**Triggers:**
- Push to `main`, `develop`, `feature/**`, `hotfix/**`, `release/**`
- Pull requests to `main` and `develop`

**Jobs:**

#### Job 1: Code Quality Checks ✅
- **Purpose**: Ensures code follows style guidelines and has no bugs
- **Steps**:
  - Checkout code
  - Set up Java 21
  - Run Checkstyle validation
  - Run SpotBugs static analysis
  - Upload reports as artifacts

#### Job 2: Build Application 🔨
- **Purpose**: Compiles the application
- **Steps**:
  - Checkout code
  - Set up Java 21 with Maven cache
  - Compile source code
  - Verify build artifacts exist
  - Upload compiled classes

#### Job 3: Unit Tests 🧪
- **Purpose**: Runs all unit tests
- **Dependencies**: Requires successful build
- **Services**: PostgreSQL 15
- **Steps**:
  - Start PostgreSQL test database
  - Run unit tests with Maven
  - Upload test results
  - Verify no test failures

#### Job 4: Code Coverage 📊
- **Purpose**: Measures test coverage
- **Dependencies**: Requires successful unit tests
- **Services**: PostgreSQL 15
- **Steps**:
  - Run tests with JaCoCo
  - Generate coverage report
  - Check 70% minimum threshold
  - Upload to Codecov (optional)

#### Job 5: Package Application 📦
- **Purpose**: Creates deployable JAR
- **Dependencies**: All previous jobs
- **Steps**:
  - Build complete application
  - Create executable JAR
  - Extract build metadata (git SHA, version)
  - Upload JAR and metadata

#### Job 6: Security Scan 🔒
- **Purpose**: Checks for vulnerabilities
- **Steps**:
  - Run Trivy security scanner
  - Check dependencies for CVEs
  - Upload results to GitHub Security

#### Job 7: CI Summary 📝
- **Purpose**: Overall pipeline status
- **Steps**:
  - Check all job results
  - Post summary comment on PR
  - Fail if any critical job failed

---

### 2. Pull Request Checks (`pr-checks.yml`)

**Triggers:**
- Pull request opened, updated, or reopened

**Validations:**
- PR title follows Conventional Commits
- No merge conflicts
- Valid branch naming convention
- Check for protected file modifications

---

### 3. Dependency Check (`dependency-check.yml`)

**Triggers:**
- Every Monday at 9 AM
- Manual trigger

**Purpose:**
- Check for available dependency updates
- Generate update report

---

## 🚀 Quick Start

### Prerequisites

1. **GitHub Repository**
   ```bash
   git remote -v
   # Should show your GitHub repository
   ```

2. **Workflows Directory** (Already created)
   ```
   .github/workflows/
   ├── ci.yml
   ├── pr-checks.yml
   └── dependency-check.yml
   ```

### Enable GitHub Actions

1. **Push workflows to GitHub**:
   ```bash
   cd /Users/hoangnguyen/LOCAL_Documents/FPT_University/Spring_2026/realvista
   git add .github/
   git commit -m "ci: add GitHub Actions CI pipeline"
   git push origin main
   ```

2. **Verify in GitHub**:
   - Go to your repository
   - Click "Actions" tab
   - You should see the workflow runs

### First Run

The CI pipeline will automatically run on:
```bash
# Push changes
git add .
git commit -m "feat: add new feature"
git push origin feature/your-feature

# Create Pull Request
# CI will run automatically
```

---

## 📊 Pipeline Status

### Success Criteria

✅ **All jobs must pass:**
- Code Quality: Checkstyle + SpotBugs pass
- Build: Compiles without errors
- Tests: All tests pass
- Coverage: Meets threshold (70%)
- Package: JAR created successfully
- Security: No critical vulnerabilities

### Failure Scenarios

❌ **Pipeline fails if:**
- Checkstyle violations detected
- Code doesn't compile
- Any test fails
- Coverage below threshold
- JAR creation fails

---

## 🔧 Configuration

### Environment Variables

Set in GitHub repository settings (Settings > Secrets and variables > Actions):

```yaml
# Optional - for external services
CODECOV_TOKEN: <your-token>        # For code coverage reporting
SONAR_TOKEN: <your-token>          # For SonarQube analysis
```

### Maven Settings

The pipeline uses:
- **Java Version**: 21
- **Distribution**: Eclipse Temurin
- **Maven Cache**: Enabled for faster builds
- **Test Database**: PostgreSQL 15

### Artifacts Retention

| Artifact | Retention | Purpose |
|----------|-----------|---------|
| Test Reports | 7 days | Debugging test failures |
| Coverage Reports | 30 days | Trend analysis |
| JAR Files | 30 days | Deployment artifacts |
| Build Metadata | 30 days | Git SHA tracking |

---

## 📈 Monitoring

### View Pipeline Status

1. **In GitHub**:
   - Repository > Actions tab
   - See all workflow runs
   - Click on run for details

2. **Status Badge** (Add to README):
   ```markdown
   ![CI](https://github.com/YOUR_USERNAME/realvista/workflows/CI%20Pipeline/badge.svg)
   ```

### Download Artifacts

```bash
# Via GitHub UI
Actions > Select Run > Scroll to Artifacts > Download

# View locally
unzip application-jar.zip
java -jar realvista-0.0.1-SNAPSHOT.jar --version
```

---

## 🛠️ Customization

### Modify Coverage Threshold

Edit `.github/workflows/ci.yml`:
```yaml
- name: Check coverage threshold
  run: |
    if [ "$COVERAGE" -lt 70 ]; then  # Change this value
      echo "Coverage below threshold"
      exit 1  # Uncomment to fail build
    fi
```

### Add Custom Steps

```yaml
- name: Custom Step
  run: |
    echo "Your custom command here"
    # Add your logic
```

### Skip CI for Certain Commits

```bash
git commit -m "docs: update README [skip ci]"
```

---

## 🐛 Troubleshooting

### Pipeline Failing?

1. **Check Logs**:
   - Actions > Failed Run > Click job > View logs

2. **Common Issues**:
   ```bash
   # Checkstyle failures
   mvn checkstyle:check
   
   # Test failures
   mvn test
   
   # Build failures
   mvn clean compile
   ```

3. **Run Locally**:
   ```bash
   # Simulate CI pipeline
   mvn clean verify
   ```

### Cache Issues

If builds are slow:
```yaml
# Clear cache by changing cache key in workflow
key: ${{ runner.os }}-m2-v2-${{ hashFiles('**/pom.xml') }}
```

---

## 📝 Branch Strategy

### Recommended Workflow

```
main (production)
  ↑
develop (integration)
  ↑
feature/* (features)
bugfix/* (bug fixes)
hotfix/* (urgent fixes)
release/* (releases)
```

### Branch Rules

Set in GitHub:
- Settings > Branches > Add rule
- **Protect `main` branch**:
  - ✅ Require pull request reviews
  - ✅ Require status checks (CI pipeline)
  - ✅ Require branches to be up to date
  - ✅ Include administrators

---

## 🎯 Best Practices

### 1. Commit Messages
```bash
feat: add user authentication
fix: resolve login bug
docs: update API documentation
test: add integration tests
chore: update dependencies
```

### 2. Pull Requests
- Keep PRs small and focused
- Write descriptive titles
- Include tests for new features
- Wait for CI to pass before merging

### 3. Code Quality
- Run checks locally before pushing
- Fix Checkstyle warnings
- Maintain test coverage
- Review SpotBugs reports

---

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [JaCoCo Coverage](https://www.jacoco.org/jacoco/trunk/doc/)

---

## 🎉 Next Steps

### Future Enhancements (when cloud is ready)

1. **CD Pipeline** (Continuous Deployment):
   ```yaml
   # Add to ci.yml when ready
   deploy-staging:
     needs: package
     runs-on: ubuntu-latest
     steps:
       - name: Deploy to staging
         run: |
           # Deploy commands here
   ```

2. **Performance Testing**:
   ```yaml
   performance-test:
     runs-on: ubuntu-latest
     steps:
       - name: Run JMeter tests
         run: |
           # Performance test commands
   ```

3. **Container Build**:
   ```yaml
   docker-build:
     runs-on: ubuntu-latest
     steps:
       - name: Build Docker image
         run: docker build -t realvista:${{ github.sha }} .
   ```

---

**Status**: ✅ CI Pipeline Ready!  
**Next Step**: Push to GitHub and watch it run!

