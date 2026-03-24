# Product Review Management with Merchant Replies

## 1. Feature Selection

**Feature Name:** Product Review Management with Merchant Replies

**Why this feature:**
- Backend APIs already exist (GET, POST, DELETE) but NO admin UI
- Real business value: merchants can respond to customer reviews professionally
- Ethical approach: All reviews visible, merchants can address concerns publicly
- Moderate scope: List view + reply functionality + detail view
- Touches both backend (add reply endpoint) and frontend (new module)
- Common e-commerce requirement (Amazon, Shopify all have this)

**Affected Areas:**
- Backend: `ProductReviewApi`, `ProductReviewService`, new `ProductReviewReply` entity
- Frontend: New reviews module under catalogue section
- Database: New `PRODUCT_REVIEW_REPLY` table

---

## 2. Feature Specification

### User Story
**As a** store administrator  
**I want to** view all product reviews and reply to them  
**So that** I can address customer concerns, thank positive reviewers, and show potential customers that we care about feedback

### Functional Requirements
- View all product reviews in a paginated list
- Filter reviews by product, rating, replied/not replied status
- See review details: customer name, product, rating (1-5 stars), description, date
- Reply to reviews with merchant comment
- Edit or delete merchant replies
- View reply status (has reply / no reply)
- Delete inappropriate reviews (spam only)

### API Changes

**New Endpoints:**
```
GET /api/v1/private/products/reviews
  Query params: page, count, productId, hasReply
  Response: { reviews: [], total: number }

POST /api/v1/private/products/reviews/{reviewId}/reply
  Body: { comment: string }
  Response: { id: number, comment: string, date: string }

PUT /api/v1/private/products/reviews/{reviewId}/reply/{replyId}
  Body: { comment: string }
  Response: 200 OK

DELETE /api/v1/private/products/reviews/{reviewId}/reply/{replyId}
  Response: 204 No Content
```

**Request/Response Structure:**
```typescript
// GET response
{
  reviews: [
    {
      id: number,
      productId: number,
      productName: string,
      customer: { id: number, name: string },
      rating: number,
      description: string,
      date: string,
      reply: {
        id: number,
        comment: string,
        date: string,
        merchantName: string
      } | null
    }
  ],
  total: number
}

// POST/PUT request
{ comment: string }
```

### Data Model Updates

**New Entity: ProductReviewReply**
```java
@Entity
@Table(name = "PRODUCT_REVIEW_REPLY")
public class ProductReviewReply {
  @Id
  @GeneratedValue
  private Long id;
  
  @ManyToOne
  @JoinColumn(name = "PRODUCT_REVIEW_ID")
  private ProductReview productReview;
  
  @Column(name = "COMMENT", length = 1000)
  private String comment;
  
  @Column(name = "MERCHANT_NAME")
  private String merchantName;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "REPLY_DATE")
  private Date replyDate;
  
  @Embedded
  private AuditSection audit;
}
```

**Update ProductReview entity:**
```java
@OneToOne(mappedBy = "productReview", cascade = CascadeType.ALL)
private ProductReviewReply reply;
```

### UI Changes

**New Components:**
1. **Reviews List Page** (`/catalogue/products/reviews`)
   - Table with columns: Product, Customer, Rating, Review Text (truncated), Date, Reply Status, Actions
   - Filter bar: Product search, Rating filter, Has Reply filter (All/Replied/Not Replied)
   - Pagination controls
   - Action buttons: Reply, View Details, Delete (spam only)

2. **Review Detail Modal**
   - Full review text
   - Customer info
   - Product info with link
   - Merchant reply section:
     - If no reply: Text area + "Post Reply" button
     - If has reply: Display reply with "Edit" and "Delete" buttons
   - Delete review button (for spam)

**User Flow:**
1. Admin navigates to Catalogue → Product Reviews
2. Sees list of all reviews with reply status badges
3. Clicks "Reply" → Modal opens with reply form
4. Types response and clicks "Post Reply" → Reply saved and displayed
5. Customer sees merchant reply on storefront below their review
6. Admin can edit/delete their reply anytime

---

## 3. Implementation Plan

### Backend

#### Step 1: Create ProductReviewReply Entity
**File:** `sm-core-model/src/main/java/com/salesmanager/core/model/catalog/product/review/ProductReviewReply.java` (new)

```java
@Entity
@Table(name = "PRODUCT_REVIEW_REPLY")
public class ProductReviewReply extends SalesManagerEntity<Long, ProductReviewReply> {
  @Id
  @GeneratedValue
  private Long id;
  
  @ManyToOne
  @JoinColumn(name = "PRODUCT_REVIEW_ID")
  private ProductReview productReview;
  
  @Column(name = "COMMENT", length = 1000)
  private String comment;
  
  @Column(name = "MERCHANT_NAME")
  private String merchantName;
  
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "REPLY_DATE")
  private Date replyDate;
  
  @Embedded
  private AuditSection audit;
  // getters/setters
}
```

#### Step 2: Update ProductReview Entity
**File:** `sm-core-model/src/main/java/com/salesmanager/core/model/catalog/product/review/ProductReview.java`

Add field:
```java
@OneToOne(mappedBy = "productReview", cascade = CascadeType.ALL)
private ProductReviewReply reply;
```

#### Step 3: Create Repository
**File:** `sm-core/src/main/java/com/salesmanager/core/business/repositories/catalog/product/review/ProductReviewReplyRepository.java` (new)

```java
public interface ProductReviewReplyRepository extends JpaRepository<ProductReviewReply, Long> {
  Optional<ProductReviewReply> findByProductReviewId(Long reviewId);
}
```

#### Step 4: Add List Endpoint
**File:** `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/product/ProductReviewApi.java`

Add method:
```java
@GetMapping("/private/products/reviews")
public ReadableProductReviewList getAllReviews(
  @RequestParam(defaultValue = "0") int page,
  @RequestParam(defaultValue = "20") int count,
  @RequestParam(required = false) Long productId,
  @RequestParam(required = false) Boolean hasReply,
  @ApiIgnore MerchantStore merchantStore
)
```

#### Step 5: Add Reply Endpoints
**File:** Same as above

Add methods:
```java
@PostMapping("/private/products/reviews/{reviewId}/reply")
public ReadableProductReviewReply createReply(
  @PathVariable Long reviewId,
  @RequestBody PersistableProductReviewReply reply,
  @ApiIgnore MerchantStore merchantStore
)

@PutMapping("/private/products/reviews/{reviewId}/reply/{replyId}")
public void updateReply(
  @PathVariable Long reviewId,
  @PathVariable Long replyId,
  @RequestBody PersistableProductReviewReply reply,
  @ApiIgnore MerchantStore merchantStore
)

@DeleteMapping("/private/products/reviews/{reviewId}/reply/{replyId}")
public void deleteReply(
  @PathVariable Long reviewId,
  @PathVariable Long replyId,
  @ApiIgnore MerchantStore merchantStore
)
```

#### Step 6: Service Layer Updates
**File:** `sm-core/src/main/java/com/salesmanager/core/business/services/catalog/product/review/ProductReviewService.java`

Add methods:
```java
Page<ProductReview> listByStore(MerchantStore store, int page, int size);
Page<ProductReview> listByProduct(Long productId, int page, int size);
Page<ProductReview> listByStoreAndReplyStatus(MerchantStore store, boolean hasReply, int page, int size);
```

**File:** `sm-core/src/main/java/com/salesmanager/core/business/services/catalog/product/review/ProductReviewServiceImpl.java`

Implement the methods using repository queries.

#### Step 7: Create Reply Service
**File:** `sm-core/src/main/java/com/salesmanager/core/business/services/catalog/product/review/ProductReviewReplyService.java` (new)

```java
public interface ProductReviewReplyService extends SalesManagerEntityService<Long, ProductReviewReply> {
  ProductReviewReply getByReviewId(Long reviewId);
}
```

**File:** `sm-core/src/main/java/com/salesmanager/core/business/services/catalog/product/review/ProductReviewReplyServiceImpl.java` (new)

Implement service.

#### Step 8: Add DTOs
**File:** `sm-shop-model/src/main/java/com/salesmanager/shop/model/catalog/product/ReadableProductReviewList.java` (new)

```java
public class ReadableProductReviewList {
  private List<ReadableProductReview> reviews;
  private long total;
}
```

**File:** `sm-shop-model/src/main/java/com/salesmanager/shop/model/catalog/product/ReadableProductReviewReply.java` (new)

```java
public class ReadableProductReviewReply {
  private Long id;
  private String comment;
  private String merchantName;
  private String date;
}
```

**File:** `sm-shop-model/src/main/java/com/salesmanager/shop/model/catalog/product/PersistableProductReviewReply.java` (new)

```java
public class PersistableProductReviewReply {
  @NotEmpty
  private String comment;
}
```

#### Step 9: Update ReadableProductReview
**File:** `sm-shop-model/src/main/java/com/salesmanager/shop/model/catalog/product/ReadableProductReview.java`

Add fields:
```java
private String productName;
private ReadableProductReviewReply reply;
```

#### Step 10: Create Populators
**File:** `sm-shop/src/main/java/com/salesmanager/shop/populator/catalog/ReadableProductReviewReplyPopulator.java` (new)

Map entity to DTO.

**File:** `sm-shop/src/main/java/com/salesmanager/shop/populator/catalog/PersistableProductReviewReplyPopulator.java` (new)

Map DTO to entity.

Update existing `ReadableProductReviewPopulator` to include reply and product name.

### Frontend

#### Step 1: Create Reviews Module
**Directory:** `shopizer-admin/src/app/pages/catalogue/products/reviews/`

Files to create:
- `reviews-list/reviews-list.component.ts`
- `reviews-list/reviews-list.component.html`
- `reviews-list/reviews-list.component.scss`
- `review-detail-modal/review-detail-modal.component.ts`
- `review-detail-modal/review-detail-modal.component.html`
- `services/review.service.ts`

#### Step 2: Create Review Service
**File:** `shopizer-admin/src/app/pages/catalogue/products/reviews/services/review.service.ts`

Methods:
```typescript
getReviews(page, count, productId?, hasReply?): Observable<any>
createReply(reviewId, comment): Observable<any>
updateReply(reviewId, replyId, comment): Observable<any>
deleteReply(reviewId, replyId): Observable<any>
deleteReview(productId, reviewId): Observable<any>
```

#### Step 3: Build Reviews List Component
**File:** `reviews-list.component.ts`

- Load reviews on init
- Implement pagination
- Implement filters (product, hasReply)
- Handle reply actions
- Open detail modal

**File:** `reviews-list.component.html`

- Use `nb-card` for container
- Use `table` for reviews
- Add reply status badges (replied=success, not replied=warning)
- Add action buttons (Reply, View Details, Delete)
- Add pagination

#### Step 4: Build Detail Modal
**File:** `review-detail-modal.component.ts`

- Accept review data as input
- Show reply form if no reply exists
- Show reply with edit/delete if reply exists
- Emit reply/edit/delete events
- Close modal on action

#### Step 5: Add Routing
**File:** `shopizer-admin/src/app/pages/catalogue/products/products-routing.module.ts`

Add route:
```typescript
{ path: 'reviews', component: ReviewsListComponent }
```

#### Step 6: Add Menu Item
**File:** `shopizer-admin/src/app/pages/pages-menu.ts`

Add under Catalogue section:
```typescript
{ title: 'Product Reviews', link: '/pages/catalogue/products/reviews' }
```

#### Step 7: Update Module
**File:** `shopizer-admin/src/app/pages/catalogue/products/products.module.ts`

Declare new components.

---

## 4. Testing Strategy

### Protect Existing Code

**Critical Flows to Protect:**
- Existing product review creation (customer-facing API)
- Product listing with review counts/ratings
- Review deletion

**Actions:**
- Run existing tests before changes
- Add integration test for existing GET `/product/{id}/reviews` endpoint

### Backend Tests

**Unit Tests:**

**File:** `sm-core/src/test/java/com/salesmanager/test/core/ProductReviewReplyServiceTest.java` (new)

Tests:
- `testCreateReply()`
- `testUpdateReply()`
- `testDeleteReply()`
- `testGetReplyByReviewId()`

**Integration Tests (MANDATORY):**

**File:** `sm-shop/src/test/java/com/salesmanager/test/shop/controller/product/ProductReviewApiTest.java` (new)

Tests:
1. **Success case:** GET `/private/products/reviews` returns paginated list
2. **Filter case:** GET with `hasReply=false` returns only reviews without replies
3. **Create reply:** POST `/private/products/reviews/{id}/reply` creates reply in DB
4. **Update reply:** PUT `/private/products/reviews/{id}/reply/{replyId}` updates reply
5. **Delete reply:** DELETE `/private/products/reviews/{id}/reply/{replyId}` removes reply
6. **Edge case:** POST reply to non-existent review returns 404
7. **Edge case:** POST second reply to same review returns 400

Mock merchant store and language, use real database (H2 test DB).

### Frontend Tests

**Component Tests:**

**File:** `reviews-list.component.spec.ts`

Tests:
- Component renders review list
- Reply button opens modal
- Filter dropdown triggers API call with hasReply param

**File:** `review-detail-modal.component.spec.ts`

Tests:
- Modal displays review data
- Reply form submits correctly
- Edit button enables reply editing
- Delete button emits delete event

**Integration Test:**

**File:** `reviews-list.component.spec.ts`

Test:
- User clicks "Reply" → Modal opens → Types comment → Submits → Success toast → List refreshed

---

## 5. Execution Plan

### Development Order

**Phase 1: Backend Foundation (Day 1)**
1. Create `ProductReviewReply` entity
2. Update `ProductReview` entity with reply relationship
3. Create `ProductReviewReplyRepository`
4. Create `ProductReviewReplyService` interface and implementation
5. Write unit tests for reply service
6. Create DTOs: `ReadableProductReviewReply`, `PersistableProductReviewReply`, `ReadableProductReviewList`
7. Update `ReadableProductReview` with new fields
8. Create populators for reply DTOs

**Phase 2: Backend API (Day 1-2)**
9. Add GET `/private/products/reviews` endpoint
10. Add POST `/private/products/reviews/{id}/reply` endpoint
11. Add PUT `/private/products/reviews/{id}/reply/{replyId}` endpoint
12. Add DELETE `/private/products/reviews/{id}/reply/{replyId}` endpoint
13. Write integration tests for all endpoints
14. Test manually with Postman/curl

**Phase 3: Frontend Service (Day 2)**
15. Create `review.service.ts`
16. Implement API methods
17. Test service in isolation

**Phase 4: Frontend UI (Day 2-3)**
18. Create reviews list component (HTML + TS)
19. Create detail modal component with reply form
20. Add routing and menu item
21. Style components

**Phase 5: Integration & Testing (Day 3)**
22. Test full flow: list → reply → refresh
23. Test edit and delete reply
24. Test filters and pagination
25. Write frontend component tests
26. Fix bugs

**Phase 6: Final Verification (Day 3)**
27. Run all backend tests
28. Run all frontend tests
29. Manual E2E test: Create review via API → See in admin → Reply → Verify in DB → Check storefront
30. Check no regressions in existing product/review features

### Definition of Done

- [ ] Backend endpoints return correct data with proper pagination
- [ ] Merchant replies persist to database
- [ ] Admin UI displays all reviews with reply status
- [ ] Reply/Edit/Delete actions work and update UI immediately
- [ ] Filters (product, hasReply) work correctly
- [ ] All integration tests pass (backend)
- [ ] All component tests pass (frontend)
- [ ] No console errors in browser
- [ ] Existing product review creation still works
- [ ] Merchant replies visible on storefront (if applicable)
- [ ] Code follows existing patterns in codebase
- [ ] No breaking changes to existing APIs

---

## Summary

This feature adds **Product Review Management with Merchant Replies** to the admin panel, allowing merchants to respond to customer reviews professionally and transparently.

**Key Benefits:**
- Ethical: All reviews remain visible (no censorship)
- Professional: Merchants can address concerns publicly
- Customer trust: Shows business cares about feedback

**Technical Requirements:**
- **Backend:** New entity, 4 endpoints, service layer, DTOs, populators, integration tests
- **Frontend:** 2 components, 1 service, routing, menu item

**Estimated effort:** 3 days for a single developer

**Value:** Essential e-commerce feature that builds customer trust and allows professional customer engagement

**Risk:** Low - uses existing patterns, backward compatible, new table (no schema changes to existing tables)
