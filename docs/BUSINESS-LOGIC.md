# Shopizer Backend - Business Logic & Domain Model

---

## Business Domains

### 1. Catalog Management

#### Entities
- **Product** - Core product entity with SKU, pricing, inventory
- **Category** - Hierarchical product categories
- **Manufacturer** - Product brands/manufacturers
- **ProductType** - Product classification
- **ProductAttribute** - Custom attributes (size, color, etc.)
- **ProductOption** - Configurable options
- **ProductVariant** - Product variations

#### Key Services
- `ProductService` - Product CRUD, search, filtering
- `CategoryService` - Category hierarchy management
- `ProductAttributeService` - Attribute management
- `ProductInventoryService` - Stock management

#### Business Logic
- Product visibility rules
- Inventory tracking (available, reserved, sold)
- Price calculations (base price, special price, discounts)
- Product relationships (related, cross-sell, up-sell)

---

### 2. Order Management

#### Entities
- **Order** - Customer order with items and totals
- **OrderProduct** - Line items in order
- **OrderTotal** - Order totals (subtotal, tax, shipping, discount)
- **OrderStatusHistory** - Order status tracking
- **ShoppingCart** - Temporary cart before checkout

#### Key Services
- `OrderService` - Order processing, status updates
- `ShoppingCartService` - Cart management
- `ShoppingCartCalculationService` - Cart totals calculation

#### Business Workflows

**Checkout Process:**
```
1. Cart Creation → Add Products
2. Apply Discounts/Coupons
3. Calculate Shipping
4. Calculate Tax
5. Payment Processing
6. Order Creation
7. Inventory Deduction
8. Order Confirmation Email
```

**Order Status Flow:**
```
ORDERED → PROCESSED → SHIPPED → DELIVERED
         ↓
      CANCELLED/REFUNDED
```

---

### 3. Customer Management

#### Entities
- **Customer** - Customer account with credentials
- **CustomerAttribute** - Custom customer fields
- **CustomerReview** - Product reviews by customers
- **CustomerOptin** - Marketing preferences
- **Address** - Billing and shipping addresses

#### Key Services
- `CustomerService` - Customer CRUD, authentication
- `CustomerReviewService` - Review management
- `CustomerOptinService` - Marketing consent

#### Business Logic
- Customer registration and login
- Password reset workflow
- Customer groups and segmentation
- Loyalty points (if enabled)
- Order history tracking

---

### 4. Merchant/Store Management

#### Entities
- **MerchantStore** - Multi-tenant store configuration
- **MerchantConfiguration** - Store-specific settings
- **Language** - Supported languages
- **Currency** - Supported currencies
- **Country/Zone** - Geographic data

#### Key Services
- `MerchantStoreService` - Store management
- `MerchantConfigurationService` - Configuration management
- `LanguageService` - Multi-language support

#### Business Logic
- Multi-tenant isolation
- Store-specific branding
- Regional settings (language, currency, timezone)
- Store-level permissions

---

### 5. Payment Processing

#### Entities
- **Transaction** - Payment transaction records
- **PaymentMethod** - Configured payment methods

#### Key Services
- `PaymentService` - Payment processing orchestration
- `TransactionService` - Transaction management

#### Payment Flow
```
1. Payment Method Selection
2. Payment Gateway Integration
3. Authorization Request
4. Capture/Void/Refund
5. Transaction Recording
6. Order Status Update
```

#### Supported Operations
- Authorize
- Capture
- Refund
- Void

---

### 6. Shipping & Fulfillment

#### Entities
- **ShippingOrigin** - Warehouse/shipping location
- **ShippingQuote** - Shipping cost quotes
- **ShippingProduct** - Shipping configuration per product

#### Key Services
- `ShippingService` - Shipping calculation
- `ShippingQuoteService` - Quote management

#### Shipping Calculation Logic
```
1. Get package dimensions/weight
2. Get origin and destination
3. Query shipping provider APIs
4. Apply shipping rules (free shipping, flat rate)
5. Return available options with costs
```

---

### 7. Tax Management

#### Entities
- **TaxClass** - Tax classification (standard, reduced, exempt)
- **TaxRate** - Tax rates by region

#### Key Services
- `TaxService` - Tax calculation
- `TaxRateService` - Tax rate management
- `TaxClassService` - Tax class management

#### Tax Calculation
```
1. Determine customer location
2. Get applicable tax rates
3. Apply tax class rules
4. Calculate tax per line item
5. Sum total tax
```

---

### 8. Content Management

#### Entities
- **Content** - CMS pages and content
- **ContentDescription** - Multi-language content
- **ContentImage** - Media files

#### Key Services
- `ContentService` - Content CRUD
- File upload/management

#### Features
- Multi-language content
- SEO metadata
- Media library
- Page templates

---

### 9. User & Permission Management

#### Entities
- **User** - Admin users
- **Group** - User groups/roles
- **Permission** - Fine-grained permissions

#### Key Services
- `UserService` - User management
- `GroupService` - Role management
- `PermissionService` - Permission management

#### Security Model
- Role-based access control (RBAC)
- Hierarchical permissions
- Store-level access control

---

## Cross-Cutting Concerns

### Search
- **OpenSearch integration** for product search
- Faceted search (category, price, attributes)
- Auto-complete suggestions

### Email
- Order confirmation
- Shipping notifications
- Password reset
- Marketing emails
- Template-based (FreeMarker)

### Caching Strategy
- **Product catalog** - Cached for performance
- **Categories** - Cached hierarchy
- **Configuration** - Cached settings
- **Session data** - Distributed cache

### Audit & Logging
- Order history
- Price changes
- Inventory movements
- User actions

---

## Business Rules Engine (Drools)

### Use Cases
- **Dynamic Pricing** - Volume discounts, time-based pricing
- **Promotions** - Buy X get Y, percentage off
- **Shipping Rules** - Free shipping thresholds
- **Tax Rules** - Complex tax scenarios

### Rule Examples
```
Rule: "Free shipping over $100"
When: Cart total > $100
Then: Set shipping cost = 0

Rule: "10% off electronics"
When: Product category = "Electronics"
Then: Apply 10% discount
```

---

## Integration Points

### Payment Gateway Integration
- Abstract payment module interface
- Provider-specific implementations
- Transaction state management

### Shipping Provider Integration
- Real-time rate calculation
- Label generation
- Tracking integration

### Email Service Integration
- SMTP (default)
- AWS SES
- Template rendering

### Storage Integration
- Local file system
- AWS S3
- Google Cloud Storage

---

## Data Flow Examples

### Product Purchase Flow
```
Customer → Browse Products → Add to Cart → Checkout
    ↓
Apply Promotions → Calculate Shipping → Calculate Tax
    ↓
Process Payment → Create Order → Update Inventory
    ↓
Send Confirmation → Update Order Status
```

### Order Fulfillment Flow
```
Order Created → Payment Confirmed → Pick Items
    ↓
Pack Order → Generate Shipping Label → Ship
    ↓
Update Tracking → Notify Customer → Mark Delivered
```

---

## Performance Considerations

- **Lazy Loading** - JPA entities loaded on demand
- **Pagination** - All list APIs support pagination
- **Caching** - Multi-level caching strategy
- **Async Processing** - Background jobs for emails, reports
- **Database Indexing** - Optimized queries with indexes
