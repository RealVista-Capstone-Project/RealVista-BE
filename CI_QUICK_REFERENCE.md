# CI Pipeline Quick Reference

## 🚀 Quick Commands

### Test Locally Before Push
```bash
# Run complete CI simulation
mvn clean verify

# Quick check
mvn checkstyle:check && mvn test
```

### Push to GitHub
```bash
# Commit with conventional format
git add .
git commit -m "feat: add new feature"
git push origin feature/my-feature

# Create PR through GitHub UI
# CI will run automatically
```

## ✅ Pipeline Jobs

| Job | Duration | Purpose |
|-----|----------|---------|
| 1️⃣ Code Quality | ~30s | Checkstyle + SpotBugs |
| 2️⃣ Build | ~45s | Compile application |
| 3️⃣ Unit Tests | ~1min | Run all tests |
| 4️⃣ Code Coverage | ~1min | Measure coverage |
| 5️⃣ Package | ~1min | Create JAR |
| 6️⃣ Security Scan | ~30s | Trivy scan |
| 7️⃣ Summary | ~5s | Report status |

**Total Time**: ~5 minutes

## 🎯 Success Criteria

✅ **Must Pass**:
- Zero Checkstyle violations
- Zero SpotBugs errors
- All tests pass
- Coverage ≥ 70%
- JAR builds successfully
- No critical vulnerabilities

## 🔍 Common Issues & Fixes

### ❌ Checkstyle Failed
```bash
# Check locally
mvn checkstyle:check

# Common issues:
# - Line too long (max 120 chars)
# - Unused imports
# - Wildcard imports
# - Missing Javadoc
```

### ❌ Tests Failed
```bash
# Run tests
mvn test

# Run specific test
mvn test -Dtest=UserControllerTest

# Debug
mvn test -X
```

### ❌ Build Failed
```bash
# Clean build
mvn clean compile

# Check dependencies
mvn dependency:tree
```

### ❌ Coverage Too Low
```bash
# Generate report
mvn jacoco:report

# View report
open target/site/jacoco/index.html

# Add more tests!
```

## 📝 Commit Message Format

```
<type>(<scope>): <subject>

Types:
  feat     - New feature
  fix      - Bug fix
  docs     - Documentation
  style    - Formatting
  refactor - Code restructuring
  test     - Adding tests
  chore    - Maintenance

Examples:
  feat(auth): add JWT authentication
  fix(user): resolve email validation bug
  docs(api): update API documentation
```

## 🌿 Branch Naming

```bash
# Feature
git checkout -b feature/user-authentication

# Bug fix
git checkout -b bugfix/fix-login-error

# Hotfix
git checkout -b hotfix/security-patch

# Release
git checkout -b release/v1.0.0
```

## 🔄 Workflow

```
1. Create branch
   git checkout -b feature/my-feature

2. Make changes
   # Code, test, commit

3. Test locally
   mvn clean verify

4. Push to GitHub
   git push origin feature/my-feature

5. Create PR
   # Via GitHub UI

6. Wait for CI
   # All checks must pass

7. Get approval
   # Requires 1 reviewer

8. Merge
   # Squash and merge
```

## 📊 View Results

### GitHub Actions
```
Repository → Actions Tab → Latest Run
```

### Download Artifacts
```
Actions → Run Details → Artifacts Section
- application-jar.zip
- test-results.zip
- coverage-report.zip
- build-metadata.zip
```

### Check Status
```bash
# Health
curl http://localhost:8080/actuator/health

# Git Info (includes SHA)
curl http://localhost:8080/actuator/info

# Full info (requires auth)
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/actuator/info
```

## 🚨 Emergency Procedures

### Failing Build Blocking PRs?
```bash
# Option 1: Fix the issue
git commit -m "fix: resolve CI issue"
git push

# Option 2: Skip CI (use sparingly!)
git commit -m "docs: update [skip ci]"
git push
```

### Need to Merge Urgently?
```
1. Contact admin
2. Use hotfix process
3. Get emergency approval
4. Fix forward (don't skip CI)
```

## 📚 Resources

- **Full Guide**: CI_PIPELINE_GUIDE.md
- **Branch Rules**: .github/BRANCH_PROTECTION.md
- **Architecture**: ARCHITECTURE.md
- **Contributing**: CONTRIBUTING.md

## 🎯 Pro Tips

1. **Run CI locally** before pushing
   ```bash
   mvn clean verify
   ```

2. **Use cache** for faster builds
   - Maven cache is automatic in CI

3. **Write good commit messages**
   - Follow Conventional Commits

4. **Keep PRs small**
   - Easier to review
   - Faster CI

5. **Monitor coverage**
   - Don't let it drop below 70%

6. **Fix CI immediately**
   - Don't let it stay red

7. **Use PR template**
   - Fill it completely

## 📞 Get Help

- **Pipeline Issues**: Check CI_PIPELINE_GUIDE.md
- **Build Errors**: Check logs in Actions tab
- **Questions**: Create GitHub issue

---

**Remember**: 
- 🟢 Green CI = Good to merge
- 🔴 Red CI = Must fix
- 🟡 Yellow CI = In progress

**Status**: `git commit -m "feat: your change"` → Push → Wait for 🟢

