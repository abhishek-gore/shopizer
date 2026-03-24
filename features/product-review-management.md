# Product Review Management in Admin Panel

## 1. Feature Selection

**Feature Name:** Product Review Management Dashboard

**Why this feature:**
- Backend APIs already exist (GET, POST, DELETE) but NO admin UI
- Real business value: merchants need to moderate customer reviews
- Moderate scope: List view + approve/reject actions + detail view
- Touches both backend (add approval status endpoint) and frontend (new module)
- Common e-commerce requirement

**Affected Areas:**
- Backend: `ProductReviewApi`, `ProductReviewService`, `ProductReview` entity
- Frontend: New reviews module under catalogue section
- Database: Add `approved` status field to reviews

---

## 2. Feature Specification

### User Story
**As a** store administrator  
**I want to** view and manage product reviews submitted by customers  
**So that** I can approve legitimate reviews and reject spam/inappropriate content before they appear on the storefront

### Functional Requirements
- View all product reviews in a paginated list
- Filter reviews by status (pending/approved/rejected), product, rating
- See review details: customer name, product, rating (1-5 stars), description, date
- Approve or reject reviews with single click
- Delete reviews permanently
- View which product the review belongs to (with link)

### API Changes

**New Endpoint:**
```
GET /api/v1/private/products/reviews
  Query params: page, count, status, productId
  Response: { reviews: [], total: number }

PUT /api/v1/private/products/reviews/{reviewId}/status
  Body: { status: "APPROVED" | "REJECTED" }
  Response: 200 OK
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
      status: number  // 0=pending, 1=approved, 2=rejected
    }
  ],
  total: number
}

// PUT request
{ status: 0 | 1 | 2 }
```

### Data Model Updates
**ProductReview entity:** Already has `status` field (Integer) - use it for approval workflow:
- 0 = Pending
- 1 = Approved  
- 2 = Rejected

No schema changes needed.

### UI Changes

**New Components:**
1. **Reviews List Page** (`/catalogue/products/reviews`)
   - Table with columns: Product, Customer, Rating, Review Text (truncated), Date, Status, Actions
   - Filter bar: Status dropdown, Product search, Rating filter
   - Pagination controls
   - Bulk actions: Approve selected, Reject selected

2. **Review Detail Modal**
   - Full review text
   - Customer info
   - Product info with link
   - Approve/Reject/Delete buttons

**User Flow:**
1. Admin navigates to Catalogue → Product Reviews
2. Sees list of all reviews with status badges
3. Clicks "Approve" → Review status changes to approved
4. Clicks review row → Modal opens with full details
5. Can delete from modal if needed

---

## 3. Implementation Plan

### Backend

#### Step 1: Add List Endpoint
**File:** `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/product/ProductReviewApi.java`

Add method:
```java
@GetMapping("/private/products/reviews")
public ReadableProductReviewList getAllReviews(
  @RequestParam(defaultValue = "0") int page,
  @RequestParam(defaultValue = "20") int count,
  @RequestParam(required = false) Integer status,
  @RequestParam(required = false) Long productId,
  @ApiIgnore MerchantStore merchantStore
)
```

#### Step 2: Add Status Update Endpoint
**File:** Same as above

Add method:
```java
@PutMapping("/private/products/reviews/{reviewId}/status")
public void updateStatus(
  @PathVariable Long reviewId,
  @RequestBody Map<String, Integer> body,
  @ApiIgnore MerchantStore merchantStore
)
```

#### Step 3: Service Layer Updates
**File:** `sm-core/src/main/java/com/salesmanager/core/business/services/catalog/product/review/ProductReviewService.java`

Add methods:
```java
Page<ProductReview> listByStore(MerchantStore store, int page, int size);
Page<ProductReview> listByStoreAndStatus(MerchantStore store, Integer status, int page, int size);
Page<ProductReview> listByProduct(Long productId, int page, int size);
```

**File:** `sm-core/src/main/java/com/salesmanager/core/business/services/catalog/product/review/ProductReviewServiceImpl.java`

Implement the methods using repository queries.

#### Step 4: Add DTO for List Response
**File:** `sm-shop-model/src/main/java/com/salesmanager/shop/model/catalog/product/ReadableProductReviewList.java` (new)

```java
public class ReadableProductReviewList {
  private List<ReadableProductReview> reviews;
  private long total;
  // getters/setters
}
```

#### Step 5: Update ReadableProductReview
**File:** `sm-shop-model/src/main/java/com/salesmanager/shop/model/catalog/product/ReadableProductReview.java`

Add fields:
```java
private String productName;
private Integer status;
```

#### Step 6: Update Populator
**File:** `sm-shop/src/main/java/com/salesmanager/shop/populator/catalog/ReadableProductReviewPopulator.java`

Map product name and status fields.

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
getReviews(page, count, status?, productId?): Observable<any>
updateStatus(reviewId, status): Observable<any>
deleteReview(productId, reviewId): Observable<any>
```

#### Step 3: Build Reviews List Component
**File:** `reviews-list.component.ts`

- Load reviews on init
- Implement pagination
- Implement filters (status, product)
- Handle approve/reject actions
- Open detail modal

**File:** `reviews-list.component.html`

- Use `nb-card` for container
- Use `table` or `nb-list` for reviews
- Add status badges (pending=warning, approved=success, rejected=danger)
- Add action buttons
- Add pagination

#### Step 4: Build Detail Modal
**File:** `review-detail-modal.component.ts`

- Accept review data as input
- Emit approve/reject/delete events
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

**File:** `sm-core/src/test/java/com/salesmanager/test/core/ProductReviewServiceTest.java` (new)

Tests:
- `testListReviewsByStore()`
- `testListReviewsByStatus()`
- `testUpdateReviewStatus()`

**Integration Tests (MANDATORY):**

**File:** `sm-shop/src/test/java/com/salesmanager/test/shop/controller/product/ProductReviewApiTest.java` (new)

Tests:
1. **Success case:** GET `/private/products/reviews` returns paginated list
2. **Filter case:** GET with `status=0` returns only pending reviews
3. **Update case:** PUT `/private/products/reviews/{id}/status` with `{status: 1}` updates DB
4. **Edge case:** PUT with invalid reviewId returns 404
5. **Failure case:** PUT with invalid status value returns 400

Mock merchant store and language, use real database (H2 test DB).

### Frontend Tests

**Component Tests:**

**File:** `reviews-list.component.spec.ts`

Tests:
- Component renders review list
- Approve button calls service with correct params
- Filter dropdown triggers API call with status param

**File:** `review-detail-modal.component.spec.ts`

Tests:
- Modal displays review data
- Delete button emits delete event

**Integration Test:**

**File:** `reviews-list.component.spec.ts`

Test:
- User clicks "Approve" → Service called → Success toast shown → List refreshed

---

## 5. Execution Plan

### Development Order

**Phase 1: Backend Foundation (Day 1)**
1. Add service methods to `ProductReviewService` interface
2. Implement in `ProductReviewServiceImpl`
3. Write unit tests for service layer
4. Create `ReadableProductReviewList` DTO
5. Update `ReadableProductReview` with new fields
6. Update populator

**Phase 2: Backend API (Day 1-2)**
7. Add GET `/private/products/reviews` endpoint
8. Add PUT `/private/products/reviews/{id}/status` endpoint
9. Write integration tests for both endpoints
10. Test manually with Postman/curl

**Phase 3: Frontend Service (Day 2)**
11. Create `review.service.ts`
12. Implement API methods
13. Test service in isolation

**Phase 4: Frontend UI (Day 2-3)**
14. Create reviews list component (HTML + TS)
15. Create detail modal component
16. Add routing and menu item
17. Style components

**Phase 5: Integration & Testing (Day 3)**
18. Test full flow: list → approve → refresh
19. Test filters and pagination
20. Write frontend component tests
21. Fix bugs

**Phase 6: Final Verification (Day 3)**
22. Run all backend tests
23. Run all frontend tests
24. Manual E2E test: Create review via API → See in admin → Approve → Verify status in DB
25. Check no regressions in existing product/review features

### Definition of Done

- [ ] Backend endpoints return correct data with proper pagination
- [ ] Review status updates persist to database
- [ ] Admin UI displays all reviews with correct status badges
- [ ] Approve/Reject actions work and update UI immediately
- [ ] Filters (status, product) work correctly
- [ ] All integration tests pass (backend)
- [ ] All component tests pass (frontend)
- [ ] No console errors in browser
- [ ] Existing product review creation still works
- [ ] Code follows existing patterns in codebase
- [ ] No breaking changes to existing APIs

---

## Summary

This feature adds **Product Review Management** to the admin panel, allowing merchants to moderate customer reviews. It requires:

- **Backend:** 2 new endpoints, 3 service methods, 1 DTO, integration tests
- **Frontend:** 2 components, 1 service, routing, menu item

**Estimated effort:** 3 days for a single developer

**Value:** Essential e-commerce feature, prevents spam/inappropriate reviews from appearing on storefront

**Risk:** Low - uses existing patterns, no schema changes, backward compatible
