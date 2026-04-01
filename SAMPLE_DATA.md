# Sample Data Initialization

## Overview

Shopizer automatically loads sample products and reviews from SQL scripts when starting with an empty database. This allows anyone cloning the repository to immediately see working data.

## What Gets Created

When the application starts with an empty database, it automatically creates:

- **1 Category**: Electronics
- **3 Products**: 
  - Premium Laptop (SKU: LAPTOP-001) - $999.99
  - Smartphone Pro (SKU: PHONE-001) - $699.99
  - Tablet Ultra (SKU: TABLET-001) - $449.99
- **3 Customers**: John Doe, Jane Smith, Mike Wilson
- **3 Product Reviews**: One review per product with ratings 5.0, 4.5, and 4.0

## How It Works

Spring Boot automatically executes `data.sql` after schema creation. The SQL script:
1. Inserts category (Electronics)
2. Inserts customers with billing/delivery addresses
3. Inserts products with descriptions, availability, and prices
4. Inserts product reviews linking products to customers

All data is persisted to the database.

## For Infrastructure Repository

When you build and upload artifacts to your infra repository:

1. The Docker image will contain the `data.sql` script
2. Anyone who clones the infra repo and runs the container will get:
   - Empty database on first run
   - Automatic population with sample data via SQL
   - Immediate access to products and reviews via API

## Accessing the Data

After startup, access via Swagger UI:
- Products: `GET /api/v1/products`
- Reviews: `GET /api/v1/products/{id}/reviews`

Or visit: http://localhost:8080/swagger-ui.html

## Implementation

The sample data is defined in:
- `sm-shop/src/main/resources/data.sql`

Spring Boot automatically executes this file after Hibernate creates the schema.
