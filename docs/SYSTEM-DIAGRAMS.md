# Shopizer - System Architecture Diagrams

---

## 1. High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         USERS                                    │
├──────────────────────┬──────────────────────────────────────────┤
│   Store Admin        │         Customers                        │
│   (Merchants)        │         (Shoppers)                       │
└──────────────────────┴──────────────────────────────────────────┘
         │                              │
         │                              │
         ▼                              ▼
┌──────────────────────┐      ┌──────────────────────┐
│   Admin Panel        │      │   Customer Shop      │
│   (Angular 11)       │      │   (React 16)         │
│   Port: 82           │      │   Port: 80           │
└──────────────────────┘      └──────────────────────┘
         │                              │
         │         REST API (JSON)      │
         └──────────────┬───────────────┘
                        ▼
         ┌──────────────────────────────┐
         │   API Gateway / Load Balancer│
         └──────────────────────────────┘
                        ▼
         ┌──────────────────────────────┐
         │   Shopizer Backend           │
         │   (Spring Boot 2.5.12)       │
         │   Port: 8080                 │
         │                              │
         │   ┌────────────────────┐    │
         │   │  REST Controllers  │    │
         │   └────────────────────┘    │
         │   ┌────────────────────┐    │
         │   │  Business Services │    │
         │   └────────────────────┘    │
         │   ┌────────────────────┐    │
         │   │  Data Repositories │    │
         │   └────────────────────┘    │
         └──────────────────────────────┘
                        ▼
         ┌──────────────────────────────┐
         │   Data & Storage Layer       │
         ├──────────────────────────────┤
         │  ┌──────────┐  ┌──────────┐ │
         │  │  MySQL   │  │OpenSearch│ │
         │  │PostgreSQL│  │          │ │
         │  └──────────┘  └──────────┘ │
         │  ┌──────────┐  ┌──────────┐ │
         │  │Infinispan│  │  AWS S3  │ │
         │  │  Cache   │  │   GCS    │ │
         │  └──────────┘  └──────────┘ │
         └──────────────────────────────┘
                        ▼
         ┌──────────────────────────────┐
         │   External Services          │
         ├──────────────────────────────┤
         │  Payment: PayPal, Stripe     │
         │  Shipping: FedEx, UPS        │
         │  Email: AWS SES, SMTP        │
         │  Maps: Google Maps API       │
         └──────────────────────────────┘
```

---

## 2. Multi-Tenant Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Shopizer Platform                         │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Store A     │    │  Store B     │    │  Store C     │
│  (Tenant 1)  │    │  (Tenant 2)  │    │  (Tenant 3)  │
├──────────────┤    ├──────────────┤    ├──────────────┤
│ - Products   │    │ - Products   │    │ - Products   │
│ - Orders     │    │ - Orders     │    │ - Orders     │
│ - Customers  │    │ - Customers  │    │ - Customers  │
│ - Config     │    │ - Config     │    │ - Config     │
└──────────────┘    └──────────────┘    └──────────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            ▼
                ┌───────────────────────┐
                │  Shared Database      │
                │  (Schema Isolation)   │
                └───────────────────────┘
```

---

## 3. Request Flow Diagram

```
Customer Request Flow:

Browser
   │
   │ 1. HTTP Request
   ▼
React App (Port 80)
   │
   │ 2. API Call (axios)
   ▼
Backend API (Port 8080)
   │
   │ 3. JWT Validation
   ▼
Spring Security Filter
   │
   │ 4. Route to Controller
   ▼
REST Controller
   │
   │ 5. Call Facade
   ▼
Facade Layer
   │
   │ 6. DTO → Entity
   ▼
Service Layer
   │
   │ 7. Business Logic
   ▼
Repository Layer
   │
   │ 8. JPA Query
   ▼
Database
   │
   │ 9. Return Data
   ▼
Service → Facade → Controller
   │
   │ 10. Entity → DTO
   ▼
JSON Response
   │
   │ 11. HTTP Response
   ▼
React App
   │
   │ 12. Update Redux Store
   ▼
UI Update
```

---

## 4. Authentication Flow

```
Admin Login Flow:

┌──────────────┐
│ Admin Panel  │
│ (Angular)    │
└──────────────┘
       │
       │ 1. POST /api/v1/private/login
       │    { username, password }
       ▼
┌──────────────────────────────┐
│ AuthenticationController     │
└──────────────────────────────┘
       │
       │ 2. Validate credentials
       ▼
┌──────────────────────────────┐
│ Spring Security              │
│ AuthenticationManager        │
└──────────────────────────────┘
       │
       │ 3. Check database
       ▼
┌──────────────────────────────┐
│ UserService                  │
└──────────────────────────────┘
       │
       │ 4. Generate JWT
       ▼
┌──────────────────────────────┐
│ JWTTokenUtil                 │
└──────────────────────────────┘
       │
       │ 5. Return token
       ▼
┌──────────────────────────────┐
│ { token: "eyJ..." }          │
└──────────────────────────────┘
       │
       │ 6. Store in localStorage
       ▼
┌──────────────────────────────┐
│ Subsequent requests include: │
│ Authorization: Bearer token  │
└──────────────────────────────┘
```

---

## 5. Order Processing Flow

```
┌──────────────┐
│   Customer   │
└──────────────┘
       │
       │ 1. Add products to cart
       ▼
┌──────────────────────────────┐
│   Shopping Cart (Redux)      │
└──────────────────────────────┘
       │
       │ 2. Proceed to checkout
       ▼
┌──────────────────────────────┐
│   Checkout Page              │
│   - Shipping address         │
│   - Shipping method          │
│   - Payment method           │
└──────────────────────────────┘
       │
       │ 3. Submit order
       ▼
┌──────────────────────────────┐
│   OrderService               │
│   - Validate cart            │
│   - Calculate totals         │
└──────────────────────────────┘
       │
       ├─────────────────┬──────────────┐
       ▼                 ▼              ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ TaxService   │  │ShippingService│ │PaymentService│
│ Calculate tax│  │Calculate cost │  │Process payment│
└──────────────┘  └──────────────┘  └──────────────┘
       │                 │              │
       └─────────────────┴──────────────┘
                         ▼
       ┌──────────────────────────────┐
       │   Create Order Entity        │
       │   - Order products           │
       │   - Order totals             │
       │   - Transaction record       │
       └──────────────────────────────┘
                         │
                         ▼
       ┌──────────────────────────────┐
       │   Update Inventory           │
       │   (Deduct quantities)        │
       └──────────────────────────────┘
                         │
                         ▼
       ┌──────────────────────────────┐
       │   Send Confirmation Email    │
       └──────────────────────────────┘
                         │
                         ▼
       ┌──────────────────────────────┐
       │   Return Order Confirmation  │
       └──────────────────────────────┘
```

---

## 6. Product Search Flow

```
┌──────────────┐
│   Customer   │
│ Search: "laptop"
└──────────────┘
       │
       │ 1. GET /api/v1/search?q=laptop
       ▼
┌──────────────────────────────┐
│   SearchController           │
└──────────────────────────────┘
       │
       │ 2. Call SearchService
       ▼
┌──────────────────────────────┐
│   SearchService              │
└──────────────────────────────┘
       │
       │ 3. Query OpenSearch
       ▼
┌──────────────────────────────┐
│   OpenSearch Cluster         │
│   - Full-text search         │
│   - Faceted search           │
│   - Relevance scoring        │
└──────────────────────────────┘
       │
       │ 4. Return results
       ▼
┌──────────────────────────────┐
│   SearchResults              │
│   - Products                 │
│   - Facets (categories, etc) │
│   - Total count              │
└──────────────────────────────┘
       │
       │ 5. Enrich with pricing
       ▼
┌──────────────────────────────┐
│   ProductService             │
│   (Get current prices)       │
└──────────────────────────────┘
       │
       │ 6. Return to frontend
       ▼
┌──────────────────────────────┐
│   React Search Results Page  │
└──────────────────────────────┘
```

---

## 7. Caching Architecture

```
┌──────────────────────────────────────────────────────┐
│                   Application                         │
└──────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ L1 Cache     │ │ L2 Cache     │ │ L3 Cache     │
│ (Hibernate   │ │ (EhCache)    │ │ (Infinispan) │
│  Session)    │ │              │ │  Distributed │
├──────────────┤ ├──────────────┤ ├──────────────┤
│ - Per txn    │ │ - Products   │ │ - Sessions   │
│ - Automatic  │ │ - Categories │ │ - Cart data  │
│              │ │ - Config     │ │ - Multi-node │
└──────────────┘ └──────────────┘ └──────────────┘
                        │
                        ▼
                ┌──────────────┐
                │   Database   │
                └──────────────┘
```

---

## 8. Deployment Architecture

```
Production Environment:

┌─────────────────────────────────────────────────────┐
│                  Load Balancer                       │
│                  (AWS ALB / NGINX)                   │
└─────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  Backend     │ │  Backend     │ │  Backend     │
│  Instance 1  │ │  Instance 2  │ │  Instance 3  │
│  (Docker)    │ │  (Docker)    │ │  (Docker)    │
└──────────────┘ └──────────────┘ └──────────────┘
        │               │               │
        └───────────────┼───────────────┘
                        ▼
        ┌───────────────────────────────┐
        │   Database Cluster            │
        ├───────────────────────────────┤
        │  ┌──────────┐  ┌──────────┐  │
        │  │ Primary  │  │ Replica  │  │
        │  │ (Write)  │  │ (Read)   │  │
        │  └──────────┘  └──────────┘  │
        └───────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              CDN (CloudFront / Cloudflare)           │
└─────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼                               ▼
┌──────────────────┐          ┌──────────────────┐
│  Admin Panel     │          │  Customer Shop   │
│  (Static Files)  │          │  (Static Files)  │
│  S3 / Storage    │          │  S3 / Storage    │
└──────────────────┘          └──────────────────┘
```

---

## 9. Security Architecture

```
┌─────────────────────────────────────────────────────┐
│                Security Layers                       │
└─────────────────────────────────────────────────────┘

Layer 1: Network Security
├── Firewall rules
├── VPC / Security groups
└── DDoS protection

Layer 2: Application Security
├── HTTPS / TLS encryption
├── CORS configuration
└── Rate limiting

Layer 3: Authentication
├── JWT tokens
├── Password hashing (BCrypt)
└── Session management

Layer 4: Authorization
├── Role-based access (RBAC)
├── Permission checks
└── Resource-level security

Layer 5: Data Security
├── SQL injection prevention (JPA)
├── XSS protection (AntiSamy)
├── Input validation
└── Output encoding

Layer 6: Audit & Monitoring
├── Access logs
├── Security events
└── Anomaly detection
```

---

## 10. Integration Architecture

```
┌─────────────────────────────────────────────────────┐
│              Shopizer Backend                        │
└─────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────────────┐
        │               │                       │
        ▼               ▼                       ▼
┌──────────────┐ ┌──────────────┐    ┌──────────────┐
│   Payment    │ │   Shipping   │    │    Email     │
│  Gateways    │ │   Providers  │    │   Services   │
├──────────────┤ ├──────────────┤    ├──────────────┤
│ - PayPal     │ │ - FedEx      │    │ - AWS SES    │
│ - Stripe     │ │ - UPS        │    │ - SMTP       │
│ - Braintree  │ │ - USPS       │    └──────────────┘
└──────────────┘ │ - CanadaPost │
                 └──────────────┘
        │               │
        ▼               ▼
┌──────────────┐ ┌──────────────┐
│   Storage    │ │   Maps       │
├──────────────┤ ├──────────────┤
│ - AWS S3     │ │ - Google Maps│
│ - GCS        │ │ - Geocoding  │
│ - Local FS   │ └──────────────┘
└──────────────┘
```

This completes the system architecture diagrams!
