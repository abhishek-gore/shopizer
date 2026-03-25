package com.salesmanager.shop.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.order.OrderService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotal;
import com.salesmanager.core.model.order.OrderType;
import com.salesmanager.core.model.order.orderstatus.OrderStatus;
import com.salesmanager.core.model.payments.PaymentType;

@Component
public class SampleOrderGenerator {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CustomerService customerService;

    public void generateSampleOrders(MerchantStore store) throws Exception {
        
        // Get first customer
        Customer customer = customerService.getListByStore(store).stream().findFirst().orElse(null);
        if (customer == null) {
            throw new Exception("No customer found in database");
        }
        
        // Create 3 sample orders
        for (int i = 1; i <= 3; i++) {
            Order order = new Order();
            order.setMerchant(store);
            order.setCustomerId(customer.getId());
            order.setStatus(OrderStatus.DELIVERED);
            order.setOrderDateFinished(new Date());
            order.setDatePurchased(new Date(System.currentTimeMillis() - (i * 7L * 24 * 60 * 60 * 1000)));
            order.setTotal(new BigDecimal(99.99 * i));
            order.setOrderType(OrderType.ORDER);
            order.setCurrencyValue(new BigDecimal(1.0));
            order.setPaymentType(PaymentType.MONEYORDER);
            order.setPaymentModuleCode("moneyorder");
            order.setShippingModuleCode("pickup");
            
            // Billing address
            order.setBilling(customer.getBilling());
            if (customer.getDelivery() != null) {
                order.setDelivery(customer.getDelivery());
            }
            
            // Order total
            OrderTotal total = new OrderTotal();
            total.setTitle("Total");
            total.setText("$" + (99.99 * i));
            total.setValue(new BigDecimal(99.99 * i));
            total.setModule("total");
            total.setOrderTotalCode("total");
            total.setSortOrder(5);
            total.setOrder(order);
            
            Set<OrderTotal> totals = new HashSet<>();
            totals.add(total);
            order.setOrderTotal(totals);
            
            orderService.create(order);
        }
    }
}
