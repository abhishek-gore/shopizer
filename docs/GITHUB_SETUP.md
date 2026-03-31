# GitHub Repository Configuration

## Branch Protection Rules

Configure via: Settings → Branches → Add rule

### Main Branch Protection

```yaml
Branch name pattern: main

Protect matching branches:
  ✅ Require a pull request before merging
     - Require approvals: 2
     - Dismiss stale pull request approvals when new commits are pushed
     - Require review from Code Owners
  
  ✅ Require status checks to pass before merging
     - Require branches to be up to date before merging
     Required checks:
       - Code Quality
       - Security Scan
       - Build & Test
  
  ✅ Require conversation resolution before merging
  
  ✅ Require signed commits
  
  ✅ Require linear history
  
  ✅ Do not allow bypassing the above settings
  
  ✅ Restrict who can push to matching branches
     - Restrict pushes that create matching branches
  
  ✅ Allow force pushes: ❌
  ✅ Allow deletions: ❌
```

## GitHub Environments

### Development
```yaml
Environment name: dev
Protection rules: None
Secrets:
  - AWS_ROLE_ARN
  - ECR_REGISTRY
```

### Staging
```yaml
Environment name: staging
Protection rules:
  - Required reviewers: 1
  - Wait timer: 0 minutes
Secrets:
  - AWS_ROLE_ARN
  - ECR_REGISTRY
URL: https://staging.shopizer.example.com
```

### Production
```yaml
Environment name: production
Protection rules:
  - Required reviewers: 2 (DevOps team)
  - Wait timer: 5 minutes
  - Deployment branches: main only
Secrets:
  - AWS_ROLE_ARN
  - ECR_REGISTRY
URL: https://shopizer.example.com
```

## Repository Secrets

Configure via: Settings → Secrets and variables → Actions

```yaml
Repository Secrets:
  - SONAR_TOKEN          # SonarCloud authentication
  - AWS_ROLE_ARN         # OIDC role for AWS access
  - ECR_REGISTRY         # ECR registry URL

Environment Secrets (per environment):
  - AWS_ROLE_ARN         # Environment-specific role
  - ECR_REGISTRY         # Environment-specific registry
```

## CODEOWNERS File

Create `.github/CODEOWNERS`:

```
# Global owners
*                           @devops-team

# Backend code
/sm-shop/**                 @backend-team @devops-team

# CI/CD pipelines
/.github/workflows/**       @devops-team
/k8s/**                     @devops-team

# Documentation
/docs/**                    @tech-writers

# Security-sensitive
/scripts/**                 @security-team @devops-team
```

## GitHub Actions Settings

Configure via: Settings → Actions → General

```yaml
Actions permissions:
  ✅ Allow all actions and reusable workflows

Workflow permissions:
  ⚪ Read repository contents and packages permissions
  ✅ Read and write permissions
  ✅ Allow GitHub Actions to create and approve pull requests

Fork pull request workflows:
  ⚪ Run workflows from fork pull requests
     (Security: Prevent secret exposure)
```

## Setup Commands

```bash
# Install GitHub CLI
brew install gh

# Authenticate
gh auth login

# Create branch protection rule
gh api repos/:owner/:repo/branches/main/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["Code Quality","Security Scan","Build & Test"]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":2}' \
  --field restrictions=null

# Create environments
gh api repos/:owner/:repo/environments/staging --method PUT
gh api repos/:owner/:repo/environments/production --method PUT

# Add secrets
gh secret set SONAR_TOKEN --body "your-token"
gh secret set AWS_ROLE_ARN --body "arn:aws:iam::123456789:role/github-actions"
```
