# Shopizer Backend - Technology Stack

**Version:** 3.2.5  
**Java:** 11 (compatible with 17)  
**Spring Boot:** 2.5.12

---

## Core Framework

### Spring Boot 2.5.12
- spring-boot-starter-web (REST APIs)
- spring-boot-starter-data-jpa (Database)
- spring-boot-starter-security (Authentication)
- spring-boot-starter-actuator (Monitoring)
- spring-boot-starter-cache (Caching)

---

## Database & Persistence

### ORM
- **Hibernate 5.x** - JPA implementation
- **Spring Data JPA** - Repository pattern
- **HikariCP** - Connection pooling

### Supported Databases
- MySQL 8.0.21
- PostgreSQL 42.2.18
- H2 (Development/Testing)
- Oracle 18.3.0.0

### Caching
- **EhCache** - Second-level cache
- **Infinispan 9.4.18** - Distributed cache

---

## Security

- **Spring Security 5.x**
- **JWT (JJWT 0.8.0)** - Token authentication
- **BCrypt** - Password hashing
- **OWASP AntiSamy 1.6.7** - XSS protection
- **Passay 1.6.0** - Password validation

---

## Search & Indexing

- **OpenSearch** (via shopizer-search-opensearch 1.0.3)
- Full-text product search
- Faceted search

---

## Business Rules

### Drools 7.32.0.Final
- kie-ci, drools-core, drools-compiler
- Rule-based pricing, promotions, tax

---

## Payment Gateways

- **PayPal** (merchantsdk 2.6.109)
- **Stripe** (stripe-java 19.5.0)
- **Braintree** (braintree-java 2.73.0)

---

## Shipping Providers

- **Canada Post** (shipping-canadapost 2.17.0)
- FedEx, UPS, USPS, Purolator

---

## Cloud Services

- **AWS S3** (aws-java-sdk-s3 1.11.640)
- **AWS SES** (aws-java-sdk-ses 1.11.640)
- **Google Cloud Storage** (google-cloud-storage 1.74.0)

---

## Utilities

### Apache Commons
- commons-lang3 3.5
- commons-io 2.7
- commons-collections4 4.1
- commons-validator 1.5.1

### Other
- **Jackson 2.13.4** - JSON processing
- **MapStruct 1.3.0** - DTO mapping
- **Google Guava 27.1** - Utilities
- **FreeMarker** - Email templates

---

## API Documentation

- **Swagger 2.9.2** (Springfox)
- Interactive API explorer at `/swagger-ui.html`

---

## Build & DevOps

- **Maven 3.x** - Build tool
- **Docker** - Containerization
- **CircleCI** - CI/CD
- **Spring Boot Actuator** - Monitoring

---

## Testing

- JUnit 5 (Vintage Engine)
- Spring Boot Test
- Spring Security Test
- H2 for integration tests
