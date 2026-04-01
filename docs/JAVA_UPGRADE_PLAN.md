# Java Upgrade Implementation Plan
## Shopizer: Java 17 → Java 21 LTS

**Project:** Shopizer E-commerce Platform  
**Current Version:** Java 17  
**Target Version:** Java 21 LTS  
**Build Tool:** Maven  
**Spring Boot:** 2.5.12 → 3.2.x (required for Java 21)  
**Deployment:** Docker containers  
**CI/CD:** GitHub Actions  

---

## 1. Pre-Upgrade Assessment

### 1.1 Current State Analysis
- [x] Java version: 17 (pom.xml shows 11, but README states 17+)
- [x] Spring Boot: 2.5.12 (EOL - must upgrade)
- [x] Build tool: Maven
- [x] Deployment: Docker (adoptopenjdk/openjdk11)
- [x] CI/CD: GitHub Actions

### 1.2 Compatibility Check

#### Critical Breaking Changes (Java 17 → 21)
- **Removed APIs:**
  - `SecurityManager` (deprecated in 17, removed in 21)
  - `Applet API` (removed)
  - `Thread.stop()`, `Thread.suspend()` (removed)
  
- **JVM Changes:**
  - G1GC is default (already in Java 17)
  - ZGC improvements
  - Virtual Threads (Project Loom) - new feature

#### Spring Boot 2.5 → 3.2 Breaking Changes
- **Jakarta EE Migration:** `javax.*` → `jakarta.*`
- **Spring Security:** Major API changes
- **Spring Data:** Repository method changes
- **Actuator:** Endpoint changes
- **Configuration:** Property changes

### 1.3 Dependency Compatibility Matrix

| Dependency | Current | Target | Status | Notes |
|------------|---------|--------|--------|-------|
| Spring Boot | 2.5.12 | 3.2.x | ⚠️ Major | Requires Jakarta EE migration |
| Hibernate | 5.x | 6.x | ⚠️ Major | Bundled with Spring Boot 3 |
| Elasticsearch | 7.5.2 | 8.x | ⚠️ Major | API changes required |
| Tomcat | 9.x | 10.x | ⚠️ Major | Jakarta EE namespace |
| JUnit | 4/5 mix | 5.x | ✅ Minor | Already partially migrated |
| Swagger | 2.x | 3.x (OpenAPI) | ⚠️ Major | API documentation changes |

### 1.4 Risk Assessment

**HIGH RISK:**
- Jakarta EE namespace migration (`javax.*` → `jakarta.*`)
- Spring Security configuration changes
- Hibernate 6.x query changes
- Elasticsearch client API changes

**MEDIUM RISK:**
- JVM option changes
- Third-party library compatibility
- Custom security implementations

**LOW RISK:**
- Language features (backward compatible)
- Build tool configuration
- Docker base image update

---

## 2. Environment Preparation

### 2.1 JDK Installation

#### Development Machines
```bash
# Install Java 21 (Temurin/Eclipse Adoptium)
# macOS
brew install openjdk@21

# Linux
wget https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1%2B12/OpenJDK21U-jdk_x64_linux_hotspot_21.0.1_12.tar.gz
tar -xzf OpenJDK21U-jdk_x64_linux_hotspot_21.0.1_12.tar.gz
sudo mv jdk-21.0.1+12 /opt/java-21

# Set JAVA_HOME
export JAVA_HOME=/opt/java-21
export PATH=$JAVA_HOME/bin:$PATH
```

#### Verification
```bash
java -version  # Should show 21.x.x
mvn -version   # Should show Java version 21
```

### 2.2 Build Configuration Updates

#### Root pom.xml
```xml
<properties>
    <!-- Update Java version -->
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    
    <!-- Update Spring Boot -->
    <spring-boot.version>3.2.3</spring-boot.version>
</properties>

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.3</version>
</parent>
```

### 2.3 IDE Configuration

#### IntelliJ IDEA
- File → Project Structure → Project SDK → 21
- File → Settings → Build, Execution, Deployment → Compiler → Java Compiler → Target bytecode version: 21

#### Eclipse
- Window → Preferences → Java → Compiler → Compiler compliance level: 21
- Project → Properties → Java Build Path → Libraries → Add JRE System Library (21)

---

## 3. Codebase Changes

### 3.1 Jakarta EE Migration (CRITICAL)

#### Automated Migration Tool
```bash
# Use OpenRewrite for automated migration
mvn org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2
```

#### Manual Changes Required

**Import Statements:**
```java
// BEFORE (javax)
import javax.persistence.*;
import javax.servlet.*;
import javax.validation.*;

// AFTER (jakarta)
import jakarta.persistence.*;
import jakarta.servlet.*;
import jakarta.validation.*;
```

**Files to Update:**
- All entity classes (`@Entity`, `@Table`, etc.)
- All REST controllers (`@RequestMapping`, servlet filters)
- All validation annotations (`@Valid`, `@NotNull`, etc.)

#### Search & Replace Script
```bash
# Create migration script
cat > migrate-jakarta.sh << 'EOF'
#!/bin/bash
find . -type f -name "*.java" -exec sed -i '' \
  -e 's/import javax\.persistence\./import jakarta.persistence./g' \
  -e 's/import javax\.servlet\./import jakarta.servlet./g' \
  -e 's/import javax\.validation\./import jakarta.validation./g' \
  -e 's/import javax\.transaction\./import jakarta.transaction./g' \
  -e 's/import javax\.annotation\./import jakarta.annotation./g' \
  {} +
EOF
chmod +x migrate-jakarta.sh
```

### 3.2 Spring Security Changes

**BEFORE (Spring Security 5.x):**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/api/public/**").permitAll()
            .anyRequest().authenticated();
    }
}
```

**AFTER (Spring Security 6.x):**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/**").permitAll()
            .anyRequest().authenticated()
        );
        return http.build();
    }
}
```

### 3.3 Hibernate 6.x Changes

**Query Changes:**
```java
// BEFORE
Query query = session.createQuery("FROM Product");
List<Product> products = query.list();

// AFTER
Query<Product> query = session.createQuery("FROM Product", Product.class);
List<Product> products = query.getResultList();
```

### 3.4 JVM Options Migration

**Remove deprecated options:**
```bash
# REMOVE these from startup scripts
-XX:+UseConcMarkSweepGC  # Removed in Java 14
-XX:+UseParNewGC          # Removed in Java 14

# ADD/UPDATE
-XX:+UseG1GC              # Default, but explicit is good
-XX:+UseZGC               # Consider for large heaps
--enable-preview          # If using virtual threads
```

---

## 4. Dependency Upgrades

### 4.1 Critical Dependencies

#### pom.xml Updates
```xml
<properties>
    <!-- Core -->
    <spring-boot.version>3.2.3</spring-boot.version>
    <hibernate.version>6.4.4.Final</hibernate.version>
    
    <!-- Search -->
    <elasticsearch.version>8.12.0</elasticsearch.version>
    
    <!-- Utilities -->
    <guava.version>33.0.0-jre</guava.version>
    <commons-lang3.version>3.14.0</commons-lang3.version>
    <commons-io.version>2.15.1</commons-io.version>
    
    <!-- API Documentation -->
    <springdoc-openapi.version>2.3.0</springdoc-openapi.version>
    
    <!-- Testing -->
    <junit-jupiter.version>5.10.2</junit-jupiter.version>
</properties>

<dependencies>
    <!-- Replace Swagger with SpringDoc OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>${springdoc-openapi.version}</version>
    </dependency>
    
    <!-- Elasticsearch -->
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-high-level-client</artifactId>
        <version>${elasticsearch.version}</version>
    </dependency>
</dependencies>
```

### 4.2 Dependency Verification

```bash
# Check for dependency conflicts
mvn dependency:tree > dependency-tree.txt
mvn dependency:analyze > dependency-analysis.txt

# Check for CVEs
mvn org.owasp:dependency-check-maven:check
```

---

## 5. Testing Strategy

### 5.1 Test Environment Setup

```bash
# Create test branch
git checkout -b feature/java-21-upgrade

# Set up parallel testing environment
export JAVA_HOME=/opt/java-21
export MAVEN_OPTS="-Xmx2g"
```

### 5.2 Testing Phases

#### Phase 1: Compilation
```bash
# Clean build
mvn clean compile

# Expected issues:
# - Jakarta namespace errors
# - Deprecated API usage
# - Spring Security configuration errors
```

#### Phase 2: Unit Tests
```bash
# Run unit tests
mvn test

# Focus areas:
# - Entity mapping tests
# - Service layer tests
# - Utility class tests
```

#### Phase 3: Integration Tests
```bash
# Run integration tests
mvn verify

# Focus areas:
# - API endpoint tests
# - Database integration
# - Security tests
# - Shopping cart operations
```

#### Phase 4: Manual Testing Checklist

- [ ] User registration and login
- [ ] Product catalog browsing
- [ ] Shopping cart operations (add, update, remove)
- [ ] Checkout process
- [ ] Order management
- [ ] Admin panel access
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Search functionality
- [ ] Payment processing
- [ ] Email notifications

### 5.3 Performance Testing

```bash
# JMeter test plan
jmeter -n -t performance-test.jmx -l results.jtl

# Key metrics:
# - Response time (should be ≤ current)
# - Throughput (should be ≥ current)
# - Memory usage
# - GC behavior
```

### 5.4 Automated Test Updates

**Update test dependencies:**
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Remove JUnit 4 -->
<!-- <dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
</dependency> -->
```

**Migrate JUnit 4 → 5:**
```java
// BEFORE
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

// AFTER
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
```

---

## 6. CI/CD Updates

### 6.1 GitHub Actions

**Update `.github/workflows/pr-validation.yml`:**
```yaml
env:
  JAVA_VERSION: '21'

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
```

### 6.2 Build Pipeline Updates

**Add version validation:**
```yaml
- name: Verify Java Version
  run: |
    java -version
    if ! java -version 2>&1 | grep -q "21"; then
      echo "❌ Java 21 required"
      exit 1
    fi
```

### 6.3 Docker Build Updates

**Update `sm-shop/Dockerfile`:**
```dockerfile
# BEFORE
FROM adoptopenjdk/openjdk11-openj9:alpine

# AFTER
FROM eclipse-temurin:21-jre-alpine

# Add JVM options for Java 21
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+UseStringDeduplication"

RUN mkdir /opt/app
RUN mkdir /files
COPY target/shopizer.jar /opt/app
COPY SALESMANAGER.h2.db /
COPY ./files /files

CMD ["java", $JAVA_OPTS, "-jar", "/opt/app/shopizer.jar"]
```

---

## 7. Deployment Considerations

### 7.1 Container Updates

#### Docker Compose
```yaml
version: '3.8'
services:
  shopizer:
    image: shopizer:java21
    build:
      context: .
      dockerfile: sm-shop/Dockerfile
    environment:
      - JAVA_OPTS=-XX:+UseG1GC -Xmx2g
      - SPRING_PROFILES_ACTIVE=production
    ports:
      - "8080:8080"
```

### 7.2 Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: shopizer
spec:
  template:
    spec:
      containers:
      - name: shopizer
        image: shopizer:java21
        env:
        - name: JAVA_OPTS
          value: "-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
```

### 7.3 Rollback Strategy

#### Blue-Green Deployment
```bash
# Deploy Java 21 version (green)
kubectl apply -f deployment-java21.yaml

# Test green deployment
curl http://shopizer-green.example.com/actuator/health

# Switch traffic if successful
kubectl patch service shopizer -p '{"spec":{"selector":{"version":"java21"}}}'

# Rollback if issues
kubectl patch service shopizer -p '{"spec":{"selector":{"version":"java17"}}}'
```

#### Feature Flag Approach
```java
@Configuration
public class VersionConfig {
    @Value("${app.java.version:17}")
    private int javaVersion;
    
    @Bean
    public boolean isJava21() {
        return javaVersion >= 21;
    }
}
```

---

## 8. Risk Management

### 8.1 Key Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Jakarta EE migration breaks production | HIGH | MEDIUM | Comprehensive testing, staged rollout |
| Spring Security changes break auth | HIGH | MEDIUM | Security audit, penetration testing |
| Performance degradation | MEDIUM | LOW | Load testing, monitoring |
| Third-party library incompatibility | MEDIUM | MEDIUM | Dependency audit, version testing |
| Database migration issues | HIGH | LOW | Backup strategy, transaction testing |

### 8.2 Rollback Plan

**Immediate Rollback (< 1 hour):**
```bash
# Revert Docker image
docker pull shopizer:java17-stable
docker-compose up -d

# Revert Kubernetes
kubectl rollout undo deployment/shopizer
```

**Code Rollback:**
```bash
# Revert Git branch
git revert <upgrade-commit-hash>
git push origin main

# Trigger CI/CD pipeline
```

### 8.3 Monitoring & Alerts

**Key Metrics:**
- JVM heap usage
- GC pause times
- API response times
- Error rates
- Database connection pool

**Alert Thresholds:**
```yaml
alerts:
  - name: HighErrorRate
    condition: error_rate > 5%
    action: rollback
  
  - name: SlowResponse
    condition: p95_latency > 2s
    action: investigate
  
  - name: HighMemory
    condition: heap_usage > 85%
    action: scale_up
```

---

## 9. Timeline and Phases

### Phase 1: Preparation (Week 1-2)
**Duration:** 2 weeks  
**Team:** 2 developers

- [ ] Set up Java 21 development environments
- [ ] Audit dependencies and create compatibility matrix
- [ ] Create feature branch
- [ ] Set up parallel CI/CD pipeline
- [ ] Document current system behavior

**Deliverables:**
- Dependency compatibility report
- Risk assessment document
- Test environment ready

### Phase 2: Core Migration (Week 3-5)
**Duration:** 3 weeks  
**Team:** 3-4 developers

- [ ] Update build configurations (pom.xml)
- [ ] Jakarta EE namespace migration
- [ ] Spring Boot 3.x upgrade
- [ ] Spring Security refactoring
- [ ] Hibernate 6.x migration
- [ ] Fix compilation errors

**Deliverables:**
- Code compiles successfully
- Unit tests pass
- Migration script documented

### Phase 3: Testing & Validation (Week 6-7)
**Duration:** 2 weeks  
**Team:** 2 developers + 1 QA

- [ ] Run full test suite
- [ ] Integration testing
- [ ] Performance testing
- [ ] Security testing
- [ ] Manual QA testing
- [ ] Fix identified issues

**Deliverables:**
- All tests passing
- Performance benchmarks met
- Security audit passed

### Phase 4: Deployment Preparation (Week 8)
**Duration:** 1 week  
**Team:** 2 developers + 1 DevOps

- [ ] Update Docker images
- [ ] Update CI/CD pipelines
- [ ] Prepare deployment scripts
- [ ] Create rollback procedures
- [ ] Update documentation

**Deliverables:**
- Deployment runbook
- Rollback procedures
- Updated documentation

### Phase 5: Staged Rollout (Week 9-10)
**Duration:** 2 weeks  
**Team:** Full team

**Week 9: Staging Deployment**
- [ ] Deploy to staging environment
- [ ] Run smoke tests
- [ ] Monitor for 3 days
- [ ] Fix any issues

**Week 10: Production Deployment**
- [ ] Deploy to production (off-peak hours)
- [ ] Monitor closely for 24 hours
- [ ] Gradual traffic increase
- [ ] Performance validation

**Deliverables:**
- Production running Java 21
- Monitoring dashboards updated
- Post-deployment report

### Phase 6: Post-Upgrade (Week 11-12)
**Duration:** 2 weeks  
**Team:** 1-2 developers

- [ ] Monitor production metrics
- [ ] Optimize performance
- [ ] Clean up legacy code
- [ ] Update team documentation
- [ ] Knowledge transfer sessions

**Deliverables:**
- Performance optimization report
- Updated developer guides
- Lessons learned document

---

## 10. Post-Upgrade Tasks

### 10.1 Performance Validation

**Baseline Metrics (Java 17):**
```bash
# Capture before upgrade
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/http.server.requests
```

**Comparison Script:**
```bash
#!/bin/bash
# compare-performance.sh

echo "=== Java 17 Baseline ==="
cat baseline-java17.json | jq '.metrics'

echo "=== Java 21 Current ==="
curl -s http://localhost:8080/actuator/metrics | jq '.metrics'

echo "=== Comparison ==="
# Add comparison logic
```

### 10.2 Monitoring Updates

**Add Java 21 specific metrics:**
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'shopizer-java21'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    metric_relabel_configs:
      - source_labels: [__name__]
        regex: 'jvm_.*'
        action: keep
```

**Grafana Dashboard:**
- JVM memory (heap, non-heap, metaspace)
- GC metrics (G1GC specific)
- Thread count (including virtual threads if used)
- API latency percentiles
- Error rates by endpoint

### 10.3 Cleanup Tasks

**Remove deprecated code:**
```bash
# Find and remove Java 11/17 specific workarounds
grep -r "TODO.*Java.*21" .
grep -r "@Deprecated" . | grep -v "target/"
```

**Update documentation:**
- [ ] README.md (Java version requirements)
- [ ] CONTRIBUTING.md (development setup)
- [ ] API documentation
- [ ] Deployment guides
- [ ] Troubleshooting guides

**Remove old configurations:**
```bash
# Remove Java 17 specific configs
rm -f .java-version-17
rm -f docker-compose-java17.yml

# Update .gitignore if needed
```

### 10.4 Knowledge Transfer

**Team Training Sessions:**
1. **Session 1:** Java 21 new features
   - Virtual Threads (Project Loom)
   - Pattern Matching enhancements
   - Record Patterns
   - Sequenced Collections

2. **Session 2:** Spring Boot 3.x changes
   - Jakarta EE migration
   - Spring Security 6.x
   - Observability improvements

3. **Session 3:** Troubleshooting
   - Common migration issues
   - Performance tuning
   - Debugging tips

**Documentation Updates:**
```markdown
# docs/JAVA_21_GUIDE.md

## New Features We're Using
- Virtual Threads for async operations
- Pattern matching in switch expressions
- Improved garbage collection

## Breaking Changes
- Jakarta EE namespace
- Spring Security configuration
- Hibernate query API

## Troubleshooting
- Common errors and solutions
- Performance tuning tips
- Rollback procedures
```

---

## 11. Checklists

### 11.1 Pre-Upgrade Checklist

- [ ] Java 21 installed on all dev machines
- [ ] IDE configured for Java 21
- [ ] Dependency compatibility verified
- [ ] Test environment set up
- [ ] Backup of current production
- [ ] Rollback plan documented
- [ ] Team trained on changes
- [ ] Stakeholders informed

### 11.2 Migration Checklist

- [ ] pom.xml updated (Java version, Spring Boot)
- [ ] All `javax.*` → `jakarta.*` imports updated
- [ ] Spring Security configuration migrated
- [ ] Hibernate queries updated
- [ ] JUnit 4 tests migrated to JUnit 5
- [ ] Swagger replaced with SpringDoc OpenAPI
- [ ] Elasticsearch client updated
- [ ] JVM options updated
- [ ] Code compiles without errors
- [ ] No deprecation warnings

### 11.3 Testing Checklist

- [ ] Unit tests pass (100%)
- [ ] Integration tests pass (100%)
- [ ] API tests pass
- [ ] Security tests pass
- [ ] Performance tests meet baseline
- [ ] Load tests successful
- [ ] Manual QA completed
- [ ] Regression testing done
- [ ] Cross-browser testing (if applicable)
- [ ] Mobile testing (if applicable)

### 11.4 Deployment Checklist

- [ ] Dockerfile updated
- [ ] Docker image built and tested
- [ ] CI/CD pipeline updated
- [ ] Staging deployment successful
- [ ] Smoke tests pass in staging
- [ ] Performance validated in staging
- [ ] Security scan passed
- [ ] Deployment runbook reviewed
- [ ] Rollback procedure tested
- [ ] Monitoring dashboards updated
- [ ] Alerts configured
- [ ] On-call team briefed

### 11.5 Post-Deployment Checklist

- [ ] Production deployment successful
- [ ] Health checks passing
- [ ] No error spikes in logs
- [ ] Performance metrics normal
- [ ] User acceptance testing
- [ ] 24-hour monitoring completed
- [ ] 1-week stability confirmed
- [ ] Documentation updated
- [ ] Team retrospective completed
- [ ] Lessons learned documented

---

## 12. Emergency Contacts & Resources

### Support Contacts
- **Java 21 Issues:** OpenJDK mailing list, Stack Overflow
- **Spring Boot 3:** Spring community forums
- **Elasticsearch:** Elastic support portal
- **Infrastructure:** DevOps team lead

### Useful Resources
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [Spring Boot 3.x Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Jakarta EE Migration](https://jakarta.ee/resources/migration/)
- [Hibernate 6 Migration Guide](https://docs.jboss.org/hibernate/orm/6.0/migration-guide/migration-guide.html)

---

## Appendix A: Quick Reference Commands

```bash
# Build
mvn clean install -DskipTests
mvn clean test
mvn clean verify

# Docker
docker build -t shopizer:java21 .
docker run -p 8080:8080 shopizer:java21

# Dependency analysis
mvn dependency:tree
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates

# Code analysis
mvn spotless:check
mvn pmd:check
mvn checkstyle:check

# Security
mvn org.owasp:dependency-check-maven:check

# Performance
mvn jmeter:jmeter
```

---

## Appendix B: Common Issues & Solutions

### Issue 1: Jakarta namespace errors
**Error:** `package javax.persistence does not exist`  
**Solution:** Run Jakarta migration script, update all imports

### Issue 2: Spring Security configuration
**Error:** `WebSecurityConfigurerAdapter cannot be resolved`  
**Solution:** Migrate to SecurityFilterChain bean pattern

### Issue 3: Hibernate query errors
**Error:** `query.list() method not found`  
**Solution:** Use `query.getResultList()` instead

### Issue 4: Docker build fails
**Error:** `Unable to find image 'adoptopenjdk/openjdk11'`  
**Solution:** Update to `eclipse-temurin:21-jre-alpine`

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-31  
**Owner:** Engineering Team  
**Reviewers:** Tech Lead, DevOps Lead, QA Lead
