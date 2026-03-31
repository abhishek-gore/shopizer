# GitHub Flow & Branching Strategy

## Branch Structure

```
main (production)
  ↑
  └── Pull Request (requires approval + checks)
       ↑
       └── feature/*, bugfix/*, hotfix/*
```

## Workflow

1. **Create feature branch** from `main`
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/add-payment-gateway
   ```

2. **Develop & commit**
   ```bash
   git add .
   git commit -m "feat: add payment gateway integration"
   git push origin feature/add-payment-gateway
   ```

3. **Open Pull Request** to `main`
   - CI/CD runs automatically
   - Code review required
   - All checks must pass

4. **Merge to main**
   - Auto-deploys to staging
   - Manual approval for production

## Branch Naming Convention

| Type | Pattern | Example | Deploys To |
|------|---------|---------|------------|
| Feature | `feature/*` | `feature/user-auth` | PR preview |
| Bugfix | `bugfix/*` | `bugfix/cart-calculation` | PR preview |
| Hotfix | `hotfix/*` | `hotfix/security-patch` | Fast-track to prod |
| Release | `release/*` | `release/v1.2.0` | Staging |

## Commit Convention (Conventional Commits)

```
<type>(<scope>): <subject>

feat(auth): add OAuth2 login
fix(cart): correct tax calculation
docs(api): update swagger annotations
test(product): add integration tests
chore(deps): upgrade spring boot to 3.2.0
```

## Protection Rules

### Main Branch
- Require pull request reviews (2 approvers)
- Require status checks to pass
- Require branches to be up to date
- No force push
- No deletion

### Status Checks Required
- ✅ Code Quality (SonarQube)
- ✅ Security Scan (Trivy)
- ✅ Unit Tests
- ✅ Integration Tests
- ✅ Build Success
