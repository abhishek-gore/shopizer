# Shopizer Backend - Architecture & Design

---

## System Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     API/REST Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Controllers  │  │   Facades    │  │   Mappers    │     │
│  │ @RestCtrl    │  │  (Business   │  │  (MapStruct) │     │
│  │              │  │ Orchestration)│  │  DTO ↔ Entity│     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Business   │  │  Validation  │  │ Transaction  │     │
│  │    Logic     │  │    Rules     │  │  Management  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Repository Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Spring Data  │  │   Custom     │  │  Query DSL   │     │
│  │     JPA      │  │  Queries     │  │              │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Domain Model                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ JPA Entities │  │    Enums     │  │    Value     │     │
│  │  (@Entity)   │  │              │  │   Objects    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

## Module Structure

### Maven Multi-Module Project

```
shopizer (parent pom.xml)
│
├── sm-core-model/
│   └── Domain entities (JPA @Entity classes)
│       ├── catalog (Product, Category, Manufacturer)
│       ├── customer (Customer, Address, Review)
│       ├── order (Order, OrderProduct, Transaction)
│       ├── merchant (MerchantStore, Configuration)
│       └── reference (Country, Zone, Currency, Language)
│
├── sm-core-modules/
│   └── Pluggable integration modules
│       ├── payment (PayPal, Stripe, Braintree)
│       ├── shipping (CanadaPost, FedEx, UPS)
│       ├── cms (S3, GCS, Local storage)
│       └── email (SMTP, AWS SES)
│
├── sm-core/
│   └── Business logic & services
│       ├── services/ (Business logic)
│       ├── repositories/ (Data access)
│       ├── modules/ (Integration implementations)
│       └── configuration/ (Spring config)
│
├── sm-shop-model/
│   └── API DTOs
│       ├── PersistableXxx (Request models)
│       └── ReadableXxx (Response models)
│
└── sm-shop/
    └── REST API & Web layer
        ├── api/ (REST controllers)
        ├── facade/ (Business orchestration)
        ├── mapper/ (DTO ↔ Entity mapping)
        ├── security/ (JWT, Spring Security)
        └── application/ (Spring Boot app)
```

---

## Design Patterns

### 1. Repository Pattern
```
Interface: ProductRepository extends JpaRepository<Product, Long>
Custom: ProductRepositoryCustom (complex queries)
Implementation: ProductRepositoryImpl
```

### 2. Service Layer Pattern
```
Interface: ProductService
Implementation: ProductServiceImpl
- @Transactional methods
- Business validation
- Exception handling
```

### 3. Facade Pattern
```
ProductFacade
- Orchestrates multiple services
- Converts DTOs ↔ Entities
- Simplifies complex operations
```

### 4. Mapper Pattern (MapStruct)
```
@Mapper
interface ProductMapper {
  ReadableProduct toReadable(Product entity);
  Product toPersistable(PersistableProduct dto);
}
```

### 5. Strategy Pattern
```
PaymentModule (interface)
├── PayPalPayment
├── StripePayment
└── BraintreePayment
```

---

## Package Structure

### sm-shop (API Layer)

```
com.salesmanager.shop
├── api/
│   ├── v1/ (API version 1)
│   │   ├── product/
│   │   ├── category/
│   │   ├── order/
│   │   ├── customer/
│   │   └── store/
│   └── v2/ (API version 2)
│
├── facade/
│   ├── product/
│   ├── category/
│   ├── order/
│   └── customer/
│
├── mapper/
│   ├── catalog/
│   ├── order/
│   └── customer/
│
├── security/
│   ├── JWTTokenUtil
│   ├── AuthenticationFilter
│   └── SecurityConfig
│
└── application/
    └── ShopApplication (main)
```

### sm-core (Business Layer)

```
com.salesmanager.core.business
├── services/
│   ├── catalog/
│   │   ├── product/
│   │   ├── category/
│   │   └── pricing/
│   ├── order/
│   ├── customer/
│   ├── merchant/
│   ├── payment/
│   ├── shipping/
│   └── tax/
│
├── repositories/
│   ├── catalog/
│   ├── order/
│   ├── customer/
│   └── merchant/
│
└── modules/
    ├── integration/
    │   ├── payment/
    │   └── shipping/
    ├── cms/
    └── email/
```

---

## Request Flow

### API Request Flow

```
1. HTTP Request
   ↓
2. Spring Security Filter Chain
   ↓ (JWT validation)
3. REST Controller (@RestController)
   ↓ (Request validation)
4. Facade Layer
   ↓ (DTO → Entity mapping)
5. Service Layer (@Transactional)
   ↓ (Business logic)
6. Repository Layer
   ↓ (JPA/Hibernate)
7. Database
   ↓
8. Response (Entity → DTO)
   ↓
9. JSON Response
```

### Example: Get Product by ID

```
GET /api/v1/products/{id}
   ↓
ProductApi.getProduct(id)
   ↓
ProductFacade.getProduct(id, store, language)
   ↓
ProductService.getById(id)
   ↓
ProductRepository.findById(id)
   ↓
ProductMapper.toReadable(product)
   ↓
ReadableProduct (JSON)
```

---

## Security Architecture

### JWT Authentication Flow

```
1. Login Request (username/password)
   ↓
2. AuthenticationController
   ↓
3. Spring Security Authentication
   ↓
4. Generate JWT Token
   ↓
5. Return Token to Client
   ↓
6. Client stores token
   ↓
7. Subsequent requests include token in header
   ↓
8. JWTAuthenticationFilter validates token
   ↓
9. Set SecurityContext
   ↓
10. Process request
```

### Security Configuration

```
Admin API: /api/v1/private/**
- Requires ADMIN role
- JWT authentication

Customer API: /api/v1/**
- Requires CUSTOMER role (for protected endpoints)
- JWT authentication

Public API: /api/v1/products, /api/v1/categories
- No authentication required
```

---

## Database Design

### Key Relationships

```
MerchantStore (1) ──→ (N) Product
MerchantStore (1) ──→ (N) Category
MerchantStore (1) ──→ (N) Customer
MerchantStore (1) ──→ (N) Order

Product (N) ──→ (N) Category (via ProductCategory)
Product (1) ──→ (N) ProductAttribute
Product (1) ──→ (N) ProductImage
Product (1) ──→ (N) ProductPrice

Order (1) ──→ (N) OrderProduct
Order (1) ──→ (N) OrderTotal
Order (1) ──→ (1) Customer
Order (1) ──→ (N) Transaction

Customer (1) ──→ (N) Address
Customer (1) ──→ (N) CustomerReview
```

---

## Caching Strategy

### Cache Levels

```
1. First-Level Cache (Hibernate Session)
   - Automatic per transaction

2. Second-Level Cache (EhCache)
   - Product catalog
   - Categories
   - Configuration

3. Query Cache
   - Frequently used queries

4. Distributed Cache (Infinispan)
   - Session data
   - Shopping cart
   - Multi-node deployments
```

---

## Error Handling

### Exception Hierarchy

```
ServiceException (base)
├── ResourceNotFoundException
├── UnauthorizedException
├── OperationNotAllowedException
└── ConversionRuntimeException
```

### Global Exception Handler

```
@RestControllerAdvice
RestErrorHandler
- Catches all exceptions
- Returns standardized error response
- Logs errors
```

---

## API Versioning

```
/api/v1/** - Version 1 (current)
/api/v2/** - Version 2 (new features)
/api/v0/** - Legacy (deprecated)
```

---

## Configuration Management

### Application Properties

```
application.properties (default)
application-dev.properties (development)
application-prod.properties (production)
application-test.properties (testing)
```

### Key Configurations
- Database connection
- Cache settings
- Security settings
- File upload limits
- Email configuration
- Payment gateway credentials

---

## Monitoring & Observability

### Spring Boot Actuator Endpoints

```
/actuator/health - Health check
/actuator/metrics - Application metrics
/actuator/info - Build information
/actuator/env - Environment properties
```

### Logging

```
Logback configuration
- Console appender (development)
- File appender (production)
- Log levels per package
```

---

## Testing Strategy

### Unit Tests
- Service layer tests
- Repository tests
- Utility tests

### Integration Tests
- API endpoint tests
- Database integration tests
- Security tests

### Test Database
- H2 in-memory database
- Test data initialization
