package com.salesmanager.shop.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.shop.model.catalog.product.ReadableProductList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
public class ProductApiIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void shouldListProducts() throws Exception {
        ResponseEntity<ReadableProductList> response = testRestTemplate.getForEntity(
            "/api/v1/products?store=DEFAULT&lang=en", 
            ReadableProductList.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void shouldReturn404ForNonExistentProduct() throws Exception {
        ResponseEntity<String> response = testRestTemplate.getForEntity(
            "/api/v1/products/999999?store=DEFAULT&lang=en", 
            String.class);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
