# Complete GitHub Flow with CI/CD

## Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     Developer Workflow                           │
└─────────────────────────────────────────────────────────────────┘

1. Create Branch
   main → feature/new-feature

2. Develop & Push
   ├─ Commit changes
   ├─ Push to GitHub
   └─ Triggers: PR Validation Workflow

3. Open Pull Request
   ├─ Code Quality Check ✓
   ├─ Security Scan ✓
   ├─ Unit Tests ✓
   ├─ Integration Tests ✓
   └─ Code Review (2 approvals required)

4. Merge to Main
   └─ Triggers: Main CI/CD Pipeline

┌─────────────────────────────────────────────────────────────────┐
│                   CI/CD Pipeline Stages                          │
└─────────────────────────────────────────────────────────────────┘

Stage 1: Quality & Security (Parallel)
├─ SonarQube Analysis
├─ Trivy SAST Scan
└─ Dependency Check

Stage 2: Build & Test
├─ Unit Tests
├─ Integration Tests
├─ Build JAR
└─ Upload Artifact

Stage 3: Containerization
├─ Build Docker Image
├─ Scan Image (Trivy)
└─ Push to ECR

Stage 4: Deployment
├─ Deploy to Staging (Auto)
│  └─ Smoke Tests
└─ Deploy to Production (Manual Approval)
   ├─ Canary Deployment (10% → 50% → 100%)
   └─ Smoke Tests

┌─────────────────────────────────────────────────────────────────┐
│                    Environment Strategy                          │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐
│   Feature    │  PR opened → Validation only
│   Branch     │  No deployment
└──────────────┘
       ↓
┌──────────────┐
│     Main     │  Merge → Auto deploy to Staging
│   (Staging)  │  URL: staging.shopizer.example.com
└──────────────┘
       ↓
┌──────────────┐
│  Production  │  Manual approval required
│   (Canary)   │  URL: shopizer.example.com
└──────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      Hotfix Flow                                 │
└─────────────────────────────────────────────────────────────────┘

1. Create hotfix branch
   main → hotfix/critical-bug

2. Fast-track pipeline
   ├─ Smoke tests only
   ├─ Build & deploy
   └─ Auto-create PR to main

3. Deploy to production
   └─ Immediate deployment

4. Merge back to main
   └─ Sync with main branch
```

## Trigger Matrix

| Event | Branch | Workflow | Deploys To |
|-------|--------|----------|------------|
| Push | `feature/*` | None | - |
| PR Open | `* → main` | PR Validation | - |
| PR Merge | `main` | Full CI/CD | Staging → Prod |
| Push | `hotfix/*` | Hotfix Pipeline | Production |
| Manual | Any | Workflow Dispatch | Selected env |

## Example Scenarios

### Scenario 1: New Feature
```bash
# Developer creates feature
git checkout -b feature/payment-integration
git commit -m "feat: add stripe payment"
git push origin feature/payment-integration

# Opens PR → CI runs validation
# After approval → Merges to main
# Auto-deploys to staging
# Manual approval for production
```

### Scenario 2: Critical Bug
```bash
# Developer creates hotfix
git checkout -b hotfix/payment-crash
git commit -m "fix: handle null payment method"
git push origin hotfix/payment-crash

# Fast-track pipeline runs
# Deploys directly to production
# Auto-creates PR to main for review
```

### Scenario 3: Rollback
```bash
# Via GitHub Actions UI
# Workflow: ci-cd-pipeline.yml
# Action: workflow_dispatch
# Select: rollback job
# Executes: kubectl argo rollouts undo
```
