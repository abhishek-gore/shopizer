package com.salesmanager.shop.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldListProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("store", "DEFAULT")
                .param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products").isArray());
    }

    @Test
    public void shouldReturnProductById() throws Exception {
        mockMvc.perform(get("/api/v1/products/1")
                .param("store", "DEFAULT")
                .param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999")
                .param("store", "DEFAULT")
                .param("lang", "en"))
                .andExpect(status().isNotFound());
    }
}
