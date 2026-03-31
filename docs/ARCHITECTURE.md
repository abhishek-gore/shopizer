# Production-Grade CI/CD Pipeline - Architecture Summary

## Overview

Complete CI/CD solution for Shopizer (Spring Boot) with GitHub Actions, AWS EKS, and canary deployments.

## Architecture Decisions

| Component | Choice | Justification |
|-----------|--------|---------------|
| CI/CD Platform | GitHub Actions | Native integration, cost-effective, mature ecosystem |
| Container Orchestration | AWS EKS | Portability, advanced deployments, rich ecosystem |
| Container Registry | AWS ECR | AWS-native, OIDC integration, cost-effective |
| Deployment Strategy | Canary (Argo Rollouts) | Gradual rollout, automatic rollback, production-safe |
| Static Analysis | SonarCloud | Free for open source, comprehensive metrics |
| Security Scanning | Trivy | Open source, fast, comprehensive CVE database |
| Versioning | Semantic + Git SHA | Traceability, uniqueness, semantic meaning |

## Pipeline Stages

```
┌─────────────────────────────────────────────────────────────┐
│ Stage 1: Quality & Security (Parallel - 3-5 min)            │
├─────────────────────────────────────────────────────────────┤
│ • SonarQube: Code quality, bugs, code smells               │
│ • Trivy SAST: Security vulnerabilities in code             │
│ • Dependency Check: CVE scanning for dependencies          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Stage 2: Build & Test (5-8 min)                            │
├─────────────────────────────────────────────────────────────┤
│ • Unit Tests: Business logic validation                    │
│ • Integration Tests: API + Database tests                  │
│ • Build JAR: Maven package with version                    │
│ • Artifact Upload: 30-day retention                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Stage 3: Containerization (3-5 min)                        │
├─────────────────────────────────────────────────────────────┤
│ • Docker Build: Multi-stage, optimized layers             │
│ • Image Scan: Trivy container vulnerability scan          │
│ • Push to ECR: Tagged with version + latest               │
│ • Cache: GitHub Actions cache for faster builds           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ Stage 4: Deployment                                         │
├─────────────────────────────────────────────────────────────┤
│ Staging (Auto - 2 min)                                     │
│ • Rolling deployment                                        │
│ • Smoke tests                                              │
│                                                             │
│ Production (Manual Approval - 10-15 min)                   │
│ • Canary: 10% → 50% → 100%                                │
│ • Automated metrics monitoring                             │
│ • Smoke tests at each stage                               │
│ • Auto-rollback on failure                                │
└─────────────────────────────────────────────────────────────┘

Total Pipeline Time: ~25-35 minutes (main → production)
```

## Versioning Strategy

```
Format: v{major}.{minor}.{patch}-{git-sha}

Examples:
  v1.0.123-a1b2c3d     (Production release)
  v0.0.45-x9y8z7-dev   (Development build)
  hotfix-payment-a1b2  (Hotfix fast-track)
```

## Environment Strategy

| Environment | Trigger | Approval | URL | Purpose |
|-------------|---------|----------|-----|---------|
| Dev | Feature branch push | None | - | Local development |
| Staging | Merge to main | None | staging.shopizer.example.com | Pre-production testing |
| Production | After staging | 2 approvers + 5min wait | shopizer.example.com | Live traffic |

## Secrets Management

**GitHub Secrets (encrypted at rest):**
- `SONAR_TOKEN`: SonarCloud authentication
- `AWS_ROLE_ARN`: OIDC role for AWS access
- `ECR_REGISTRY`: Container registry URL

**AWS Secrets Manager (runtime):**
- Database credentials
- API keys
- Third-party service tokens

**Kubernetes Secrets:**
- Mounted as environment variables
- Rotated via external-secrets operator

## Rollback Strategy

### Automated Rollback
```yaml
Canary deployment monitors:
  - Error rate > 5%
  - Response time > 2s
  - Failed health checks

Action: Automatic rollback to previous version
Time: < 30 seconds
```

### Manual Rollback
```bash
# Via GitHub Actions
Workflow: ci-cd-pipeline.yml
Job: rollback
Trigger: workflow_dispatch

# Via kubectl
kubectl argo rollouts undo shopizer-backend -n production
```

## Performance Optimizations

1. **Parallel Jobs**: Quality checks run simultaneously
2. **Maven Cache**: Dependencies cached between runs (~2min saved)
3. **Docker Layer Cache**: GitHub Actions cache (~3min saved)
4. **Artifact Reuse**: JAR built once, used in Docker build
5. **Multi-stage Docker**: Smaller images, faster pulls

## Artifact Retention

| Artifact Type | Retention | Storage |
|---------------|-----------|---------|
| JAR files | 30 days | GitHub Artifacts |
| Docker images | 90 days | ECR Lifecycle Policy |
| Build logs | 90 days | GitHub Actions |
| Test reports | 30 days | GitHub Artifacts |

## Security Features

- ✅ SAST scanning (Trivy)
- ✅ Dependency vulnerability scanning
- ✅ Container image scanning
- ✅ SARIF upload to GitHub Security
- ✅ Signed commits required
- ✅ OIDC authentication (no long-lived credentials)
- ✅ Least privilege IAM roles
- ✅ Secrets rotation via AWS Secrets Manager

## Monitoring & Observability

**Deployment Metrics:**
- Deployment frequency
- Lead time for changes
- Mean time to recovery (MTTR)
- Change failure rate

**Application Metrics:**
- Response time
- Error rate
- Request throughput
- Resource utilization

## Cost Optimization

| Resource | Strategy | Savings |
|----------|----------|---------|
| GitHub Actions | Cache dependencies, parallel jobs | ~40% faster |
| ECR Storage | Lifecycle policies (90 days) | ~60% reduction |
| EKS Compute | Spot instances for dev/staging | ~70% cost reduction |
| Build Time | Incremental builds, layer caching | ~50% faster |

## Disaster Recovery

**Backup Strategy:**
- Database: Automated daily snapshots (35-day retention)
- Configuration: GitOps (all config in Git)
- Secrets: AWS Secrets Manager with cross-region replication

**Recovery Time Objective (RTO):** < 15 minutes
**Recovery Point Objective (RPO):** < 1 hour

## Files Created

```
.github/workflows/
├── ci-cd-pipeline.yml       # Main pipeline
├── pr-validation.yml        # PR checks
└── hotfix-pipeline.yml      # Fast-track hotfixes

sm-shop/src/test/
├── java/com/salesmanager/shop/
│   ├── integration/ProductApiIT.java
│   └── smoke/SmokeTest.java
└── resources/application-test.yml

scripts/
└── smoke-test.sh            # Post-deployment validation

k8s/
└── smoke-test-job.yaml      # In-cluster smoke tests

docs/
├── GITHUB_FLOW.md           # Branching strategy
├── CI_CD_FLOW.md            # Complete flow diagram
└── GITHUB_SETUP.md          # Repository configuration
```

## Quick Start

1. **Configure GitHub Secrets**
   ```bash
   gh secret set SONAR_TOKEN --body "your-token"
   gh secret set AWS_ROLE_ARN --body "arn:aws:iam::123:role/github-actions"
   gh secret set ECR_REGISTRY --body "123.dkr.ecr.us-east-1.amazonaws.com"
   ```

2. **Set Branch Protection**
   ```bash
   # See docs/GITHUB_SETUP.md for complete setup
   ```

3. **Create Feature Branch**
   ```bash
   git checkout -b feature/my-feature
   git commit -m "feat: add new feature"
   git push origin feature/my-feature
   ```

4. **Open PR → Merge → Auto-deploy**

## Assumptions

1. Spring Boot application with Maven
2. AWS account with EKS cluster provisioned
3. Argo Rollouts installed in EKS
4. GitHub repository with Actions enabled
5. SonarCloud account configured
6. AWS OIDC provider configured for GitHub Actions
7. GitHub Environments (dev, staging, production) created
8. ECR repositories created

## Next Steps

- [ ] Configure GitHub repository settings
- [ ] Set up AWS infrastructure (EKS, ECR)
- [ ] Install Argo Rollouts in EKS
- [ ] Configure SonarCloud project
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure alerting (PagerDuty/Slack)
- [ ] Document runbooks for common scenarios
