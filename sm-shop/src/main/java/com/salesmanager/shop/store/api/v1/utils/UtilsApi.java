package com.salesmanager.shop.store.api.v1.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.utils.SampleOrderGenerator;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/v1/utils")
public class UtilsApi {

    @Autowired
    private SampleOrderGenerator sampleOrderGenerator;
    
    @Autowired
    private StoreFacade storeFacade;

    @GetMapping("/generate-sample-orders")
    @ApiImplicitParams({ 
        @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT")
    })
    public String generateSampleOrders(@ApiIgnore MerchantStore merchantStore) {
        try {
            sampleOrderGenerator.generateSampleOrders(merchantStore);
            return "Successfully created 3 sample orders";
        } catch (Exception e) {
            return "Error creating sample orders: " + e.getMessage();
        }
    }
}
