# Sample Data Initialization

## Overview

Shopizer automatically initializes with sample products and reviews when starting with an empty database. This allows anyone cloning the repository to immediately see working data.

## What Gets Created

When the application starts with an empty database, it automatically creates:

- **1 Category**: Electronics
- **3 Products**: 
  - Premium Laptop (SKU: LAPTOP-001) - $999.99
  - Smartphone Pro (SKU: PHONE-001) - $699.99
  - Tablet Ultra (SKU: TABLET-001) - $449.99
- **3 Customers**: John Doe, Jane Smith, Mike Wilson
- **3 Product Reviews**: One review per product with ratings 5.0, 4.5, and 4.0

## Configuration

Sample data initialization is controlled by the property:

```properties
db.init.sample.data=true
```

### To Enable Sample Data (default)
Add to `application.properties`:
```properties
db.init.sample.data=true
```

### To Disable Sample Data
Add to `application.properties`:
```properties
db.init.sample.data=false
```

## How It Works

1. On application startup, `InitSampleProductsAndReviews` component runs
2. Checks if sample data initialization is enabled
3. Checks if products already exist (skips if data exists)
4. Creates category → customers → products → reviews in sequence
5. All data is persisted to the database

## For Infrastructure Repository

When you build and upload artifacts to your infra repository:

1. The Docker image will contain this initialization code
2. Anyone who clones the infra repo and runs the container will get:
   - Empty database on first run
   - Automatic population with sample data
   - Immediate access to products and reviews via API

## Accessing the Data

After startup, access via Swagger UI:
- Products: `GET /api/v1/products`
- Reviews: `GET /api/v1/products/{id}/reviews`

Or visit: http://localhost:8080/swagger-ui.html

## Implementation

The initialization is handled by:
- `sm-shop/src/main/java/com/salesmanager/shop/init/data/InitSampleProductsAndReviews.java`

This component uses Spring's `@PostConstruct` with `@Order(100)` to ensure it runs after the main database initialization.
