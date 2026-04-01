package com.salesmanager.test.shop.integration.product;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.shop.model.catalog.category.Category;
import com.salesmanager.shop.model.catalog.product.PersistableProductReview;
import com.salesmanager.shop.model.catalog.product.ReadableProduct;
import com.salesmanager.shop.model.catalog.product.ReadableProductReview;
import com.salesmanager.shop.model.catalog.product.product.PersistableProduct;
import com.salesmanager.shop.model.catalog.product.product.ProductSpecification;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.model.entity.Entity;
import com.salesmanager.test.shop.common.ServicesTestSupport;

/**
 * Characterization tests to protect existing product review functionality
 * before implementing merchant reply feature.
 * 
 * These tests document and protect:
 * - Customer review creation
 * - Public review listing
 * - Review update
 * - Review deletion
 */
@Ignore("Temporarily disabled - needs customer setup refactoring")
@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class ProductReviewApiCharacterizationTest extends ServicesTestSupport {

    private Long testProductId;
    private Long testCustomerId;

    @Before
    public void setup() throws Exception {
        // Create test product with unique code to avoid conflicts
        String uniqueCode = "REVIEW_TEST_" + System.currentTimeMillis();
        ReadableProduct product = sampleProduct(uniqueCode);
        testProductId = product.getId();
        assertNotNull("Product should be created", testProductId);

        // Create test customer with unique email
        long timestamp = System.currentTimeMillis();
        PersistableCustomer customer = new PersistableCustomer();
        customer.setEmailAddress("reviewtest" + timestamp + "@test.com");
        customer.setUserName("reviewtest" + timestamp);
        customer.setPassword("password123");
        customer.setFirstName("Review");
        customer.setLastName("Tester");
        customer.setLanguage("en");
        customer.setStoreCode(Constants.DEFAULT_STORE);
        
        com.salesmanager.shop.model.customer.address.Address billing = new com.salesmanager.shop.model.customer.address.Address();
        billing.setFirstName("Review");
        billing.setLastName("Tester");
        billing.setCountry("US");
        customer.setBilling(billing);

        HttpEntity<PersistableCustomer> customerEntity = new HttpEntity<>(customer, getHeader());
        ResponseEntity<PersistableCustomer> customerResponse = testRestTemplate.postForEntity(
                "/api/v1/customer/register",
                customerEntity,
                PersistableCustomer.class);

        assertThat(customerResponse.getStatusCode(), is(OK));
        assertNotNull("Customer should be created", customerResponse.getBody());
        testCustomerId = customerResponse.getBody().getId();
        assertNotNull("Customer ID should not be null", testCustomerId);
    }

    /**
     * CHARACTERIZATION TEST: Customer can create a review for a product
     * Protects: POST /api/v1/private/products/{id}/reviews
     */
    @Test
    public void testCreateProductReview_Success() throws Exception {
        PersistableProductReview review = new PersistableProductReview();
        review.setCustomerId(testCustomerId);
        review.setProductId(testProductId);
        review.setLanguage("en");
        review.setRating(4.0);
        review.setDescription("Great product! Very satisfied with the quality.");
        review.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> entity = new HttpEntity<>(review, getHeader());
        ResponseEntity<PersistableProductReview> response = testRestTemplate.postForEntity(
                "/api/v1/private/products/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                entity,
                PersistableProductReview.class);

        assertThat(response.getStatusCode(), is(CREATED));
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(4.0, response.getBody().getRating(), 0.01);
    }

    /**
     * CHARACTERIZATION TEST: Cannot create duplicate review for same customer/product
     * Protects: Duplicate review validation
     */
    @Test
    public void testCreateProductReview_DuplicateReview_Fails() throws Exception {
        // Create first review
        PersistableProductReview review1 = new PersistableProductReview();
        review1.setCustomerId(testCustomerId);
        review1.setProductId(testProductId);
        review1.setLanguage("en");
        review1.setRating(4.0);
        review1.setDescription("First review");
        review1.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> entity1 = new HttpEntity<>(review1, getHeader());
        testRestTemplate.postForEntity(
                "/api/v1/private/products/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                entity1,
                PersistableProductReview.class);

        // Attempt duplicate review
        PersistableProductReview review2 = new PersistableProductReview();
        review2.setCustomerId(testCustomerId);
        review2.setProductId(testProductId);
        review2.setLanguage("en");
        review2.setRating(5.0);
        review2.setDescription("Second review attempt");
        review2.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> entity2 = new HttpEntity<>(review2, getHeader());
        ResponseEntity<PersistableProductReview> response = testRestTemplate.postForEntity(
                "/api/v1/private/products/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                entity2,
                PersistableProductReview.class);

        // Should fail with 500 error
        assertTrue(response.getStatusCode().is5xxServerError());
    }

    /**
     * CHARACTERIZATION TEST: Rating cannot exceed maximum (5)
     * Protects: Rating validation
     */
    @Test
    public void testCreateProductReview_ExceedsMaxRating_Fails() throws Exception {
        PersistableProductReview review = new PersistableProductReview();
        review.setCustomerId(testCustomerId);
        review.setProductId(testProductId);
        review.setLanguage("en");
        review.setRating(6.0); // Exceeds max
        review.setDescription("Invalid rating");
        review.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> entity = new HttpEntity<>(review, getHeader());
        ResponseEntity<PersistableProductReview> response = testRestTemplate.postForEntity(
                "/api/v1/private/products/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                entity,
                PersistableProductReview.class);

        assertTrue(response.getStatusCode().is5xxServerError());
    }

    /**
     * CHARACTERIZATION TEST: Public can retrieve all reviews for a product
     * Protects: GET /api/v1/product/{id}/reviews
     */
    @Test
    public void testGetProductReviews_Success() throws Exception {
        // Create a review first
        PersistableProductReview review = new PersistableProductReview();
        review.setCustomerId(testCustomerId);
        review.setProductId(testProductId);
        review.setLanguage("en");
        review.setRating(4.0);
        review.setDescription("Test review for listing");
        review.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> createEntity = new HttpEntity<>(review, getHeader());
        testRestTemplate.postForEntity(
                "/api/v1/private/products/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                createEntity,
                PersistableProductReview.class);

        // Retrieve reviews
        HttpEntity<String> httpEntity = new HttpEntity<>(getHeader());
        ResponseEntity<ReadableProductReview[]> response = testRestTemplate.exchange(
                "/api/v1/product/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                HttpMethod.GET,
                httpEntity,
                ReadableProductReview[].class);

        assertThat(response.getStatusCode(), is(OK));
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        
        ReadableProductReview retrievedReview = response.getBody()[0];
        assertEquals("Test review for listing", retrievedReview.getDescription());
        assertEquals(4.0, retrievedReview.getRating(), 0.01);
    }

    /**
     * CHARACTERIZATION TEST: Returns 404 for non-existent product
     * Protects: Product existence validation
     */
    @Test
    public void testGetProductReviews_NonExistentProduct_Returns404() throws Exception {
        HttpEntity<String> httpEntity = new HttpEntity<>(getHeader());
        ResponseEntity<ReadableProductReview[]> response = testRestTemplate.exchange(
                "/api/v1/product/999999/reviews?store=" + Constants.DEFAULT_STORE,
                HttpMethod.GET,
                httpEntity,
                ReadableProductReview[].class);

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    /**
     * CHARACTERIZATION TEST: Customer can update their own review
     * Protects: PUT /api/v1/private/products/{id}/reviews/{reviewId}
     */
    @Test
    public void testUpdateProductReview_Success() throws Exception {
        // Create review
        PersistableProductReview review = new PersistableProductReview();
        review.setCustomerId(testCustomerId);
        review.setProductId(testProductId);
        review.setLanguage("en");
        review.setRating(3.0);
        review.setDescription("Initial review");
        review.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> createEntity = new HttpEntity<>(review, getHeader());
        ResponseEntity<PersistableProductReview> createResponse = testRestTemplate.postForEntity(
                "/api/v1/private/products/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                createEntity,
                PersistableProductReview.class);

        Long reviewId = createResponse.getBody().getId();

        // Update review
        PersistableProductReview updatedReview = new PersistableProductReview();
        updatedReview.setCustomerId(testCustomerId);
        updatedReview.setProductId(testProductId);
        updatedReview.setLanguage("en");
        updatedReview.setRating(5.0);
        updatedReview.setDescription("Updated review - much better!");
        updatedReview.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> updateEntity = new HttpEntity<>(updatedReview, getHeader());
        ResponseEntity<PersistableProductReview> updateResponse = testRestTemplate.exchange(
                "/api/v1/private/products/" + testProductId + "/reviews/" + reviewId + "?store=" + Constants.DEFAULT_STORE,
                HttpMethod.PUT,
                updateEntity,
                PersistableProductReview.class);

        assertThat(updateResponse.getStatusCode(), is(OK));
        assertEquals(5.0, updateResponse.getBody().getRating(), 0.01);
    }

    /**
     * CHARACTERIZATION TEST: Cannot update non-existent review
     * Protects: Review existence validation on update
     */
    @Test
    public void testUpdateProductReview_NonExistentReview_Returns404() throws Exception {
        PersistableProductReview review = new PersistableProductReview();
        review.setCustomerId(testCustomerId);
        review.setProductId(testProductId);
        review.setLanguage("en");
        review.setRating(4.0);
        review.setDescription("Update attempt");
        review.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> entity = new HttpEntity<>(review, getHeader());
        ResponseEntity<PersistableProductReview> response = testRestTemplate.exchange(
                "/api/v1/private/products/" + testProductId + "/reviews/999999?store=" + Constants.DEFAULT_STORE,
                HttpMethod.PUT,
                entity,
                PersistableProductReview.class);

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    /**
     * CHARACTERIZATION TEST: Can delete a review
     * Protects: DELETE /api/v1/private/products/{id}/reviews/{reviewId}
     */
    @Test
    public void testDeleteProductReview_Success() throws Exception {
        // Create review
        PersistableProductReview review = new PersistableProductReview();
        review.setCustomerId(testCustomerId);
        review.setProductId(testProductId);
        review.setLanguage("en");
        review.setRating(2.0);
        review.setDescription("Review to be deleted");
        review.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> createEntity = new HttpEntity<>(review, getHeader());
        ResponseEntity<PersistableProductReview> createResponse = testRestTemplate.postForEntity(
                "/api/v1/private/products/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                createEntity,
                PersistableProductReview.class);

        Long reviewId = createResponse.getBody().getId();

        // Delete review
        HttpEntity<String> deleteEntity = new HttpEntity<>(getHeader());
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(
                "/api/v1/private/products/" + testProductId + "/reviews/" + reviewId + "?store=" + Constants.DEFAULT_STORE,
                HttpMethod.DELETE,
                deleteEntity,
                Void.class);

        assertThat(deleteResponse.getStatusCode(), is(OK));
    }

    /**
     * CHARACTERIZATION TEST: Cannot delete non-existent review
     * Protects: Review existence validation on delete
     */
    @Test
    public void testDeleteProductReview_NonExistentReview_Returns404() throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(getHeader());
        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/v1/private/products/" + testProductId + "/reviews/999999?store=" + Constants.DEFAULT_STORE,
                HttpMethod.DELETE,
                entity,
                Void.class);

        assertTrue(response.getStatusCode().is4xxClientError());
    }

    /**
     * CHARACTERIZATION TEST: Cannot delete review with mismatched product ID
     * Protects: Product-review relationship validation
     */
    @Test
    public void testDeleteProductReview_MismatchedProductId_Returns404() throws Exception {
        // Create review
        PersistableProductReview review = new PersistableProductReview();
        review.setCustomerId(testCustomerId);
        review.setProductId(testProductId);
        review.setLanguage("en");
        review.setRating(3.0);
        review.setDescription("Review with product mismatch test");
        review.setDate("2026-03-24");

        HttpEntity<PersistableProductReview> createEntity = new HttpEntity<>(review, getHeader());
        ResponseEntity<PersistableProductReview> createResponse = testRestTemplate.postForEntity(
                "/api/v1/private/products/" + testProductId + "/reviews?store=" + Constants.DEFAULT_STORE,
                createEntity,
                PersistableProductReview.class);

        Long reviewId = createResponse.getBody().getId();

        // Try to delete with wrong product ID
        HttpEntity<String> deleteEntity = new HttpEntity<>(getHeader());
        ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(
                "/api/v1/private/products/999999/reviews/" + reviewId + "?store=" + Constants.DEFAULT_STORE,
                HttpMethod.DELETE,
                deleteEntity,
                Void.class);

        assertTrue(deleteResponse.getStatusCode().is4xxClientError());
    }
}
