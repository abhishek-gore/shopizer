package com.salesmanager.shop.init.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.salesmanager.core.business.services.catalog.category.CategoryService;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.manufacturer.ManufacturerService;
import com.salesmanager.core.business.services.catalog.product.review.ProductReviewService;
import com.salesmanager.core.business.services.catalog.product.type.ProductTypeService;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.country.CountryService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.reference.zone.ZoneService;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.category.CategoryDescription;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer;
import com.salesmanager.core.model.catalog.product.price.ProductPrice;
import com.salesmanager.core.model.catalog.product.price.ProductPriceDescription;
import com.salesmanager.core.model.catalog.product.review.ProductReview;
import com.salesmanager.core.model.catalog.product.review.ProductReviewDescription;
import com.salesmanager.core.model.catalog.product.type.ProductType;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerGender;
import com.salesmanager.core.model.customer.attribute.Billing;
import com.salesmanager.core.model.customer.attribute.Delivery;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;

@Component
@Order(100)
public class InitSampleProductsAndReviews {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitSampleProductsAndReviews.class);

    @Value("${db.init.sample.data:true}")
    private boolean initSampleData;

    @Inject
    private ProductService productService;

    @Inject
    private CategoryService categoryService;

    @Inject
    private CustomerService customerService;

    @Inject
    private ProductReviewService productReviewService;

    @Inject
    private MerchantStoreService merchantService;

    @Inject
    private LanguageService languageService;

    @Inject
    private ManufacturerService manufacturerService;

    @Inject
    private ProductTypeService productTypeService;

    @Inject
    private CountryService countryService;

    @Inject
    private ZoneService zoneService;

    @PostConstruct
    public void init() {
        if (!initSampleData) {
            LOGGER.info("Sample data initialization disabled");
            return;
        }

        try {
            MerchantStore store = merchantService.getByCode(MerchantStore.DEFAULT_STORE);
            if (store == null) {
                LOGGER.warn("Default store not found. Skipping sample data.");
                return;
            }

            if (productService.count(store) > 0) {
                LOGGER.info("Products already exist. Skipping sample data.");
                return;
            }

            LOGGER.info("Initializing sample products and reviews...");
            Language en = languageService.getByCode("en");
            
            Category category = createCategory(store, en);
            Customer[] customers = createCustomers(store, en);
            Product[] products = createProducts(store, en, category);
            createReviews(products, customers, en);
            
            LOGGER.info("✓ Sample data initialized: {} products, {} customers, {} reviews", 
                products.length, customers.length, products.length);
        } catch (Exception e) {
            LOGGER.error("Error initializing sample data", e);
        }
    }

    private Category createCategory(MerchantStore store, Language language) throws Exception {
        Category category = new Category();
        category.setMerchantStore(store);
        category.setCode("electronics");
        category.setSortOrder(0);
        category.setVisible(true);

        CategoryDescription desc = new CategoryDescription();
        desc.setLanguage(language);
        desc.setName("Electronics");
        desc.setDescription("Electronic products and gadgets");
        desc.setCategory(category);
        category.getDescriptions().add(desc);

        categoryService.create(category);
        LOGGER.info("✓ Category created: Electronics");
        return category;
    }

    private Customer[] createCustomers(MerchantStore store, Language language) throws Exception {
        Country country = countryService.getByCode("CA");
        Zone zone = zoneService.getByCode("QC");
        
        String[][] customerData = {
            {"john.doe@example.com", "John", "Doe"},
            {"jane.smith@example.com", "Jane", "Smith"},
            {"mike.wilson@example.com", "Mike", "Wilson"}
        };

        Customer[] customers = new Customer[customerData.length];
        for (int i = 0; i < customerData.length; i++) {
            Customer customer = new Customer();
            customer.setMerchantStore(store);
            customer.setEmailAddress(customerData[i][0]);
            customer.setGender(CustomerGender.M);
            customer.setAnonymous(false);
            customer.setDefaultLanguage(language);

            Billing billing = new Billing();
            billing.setFirstName(customerData[i][1]);
            billing.setLastName(customerData[i][2]);
            billing.setAddress("123 Main St");
            billing.setCity("Montreal");
            billing.setPostalCode("H1H1H1");
            billing.setCountry(country);
            billing.setZone(zone);
            customer.setBilling(billing);

            Delivery delivery = new Delivery();
            delivery.setAddress("123 Main St");
            delivery.setCity("Montreal");
            delivery.setPostalCode("H1H1H1");
            delivery.setCountry(country);
            delivery.setZone(zone);
            customer.setDelivery(delivery);

            customerService.create(customer);
            customers[i] = customer;
            LOGGER.info("✓ Customer created: {} {}", customerData[i][1], customerData[i][2]);
        }
        return customers;
    }

    private Product[] createProducts(MerchantStore store, Language language, Category category) throws Exception {
        Manufacturer manufacturer = manufacturerService.getByCode(store, "DEFAULT");
        ProductType productType = productTypeService.getByCode(ProductType.GENERAL_TYPE);
        Date date = new Date();

        String[][] productData = {
            {"LAPTOP-001", "Premium Laptop", "High-performance laptop with 16GB RAM and 512GB SSD", "999.99", "50"},
            {"PHONE-001", "Smartphone Pro", "Latest smartphone with advanced camera and 5G connectivity", "699.99", "100"},
            {"TABLET-001", "Tablet Ultra", "10-inch tablet with stunning display and long battery life", "449.99", "75"}
        };

        Product[] products = new Product[productData.length];
        for (int i = 0; i < productData.length; i++) {
            Product product = new Product();
            product.setSku(productData[i][0]);
            product.setManufacturer(manufacturer);
            product.setType(productType);
            product.setMerchantStore(store);
            product.setDateAvailable(date);
            product.setAvailable(true);

            ProductDescription desc = new ProductDescription();
            desc.setName(productData[i][1]);
            desc.setDescription(productData[i][2]);
            desc.setLanguage(language);
            desc.setProduct(product);
            product.getDescriptions().add(desc);

            product.getCategories().add(category);

            ProductAvailability availability = new ProductAvailability();
            availability.setProductDateAvailable(date);
            availability.setProductQuantity(Integer.parseInt(productData[i][4]));
            availability.setRegion("*");
            availability.setProduct(product);

            ProductPrice price = new ProductPrice();
            price.setDefaultPrice(true);
            price.setProductPriceAmount(new BigDecimal(productData[i][3]));
            price.setProductAvailability(availability);

            ProductPriceDescription priceDesc = new ProductPriceDescription();
            priceDesc.setName("Base price");
            priceDesc.setProductPrice(price);
            priceDesc.setLanguage(language);
            price.getDescriptions().add(priceDesc);

            availability.getPrices().add(price);
            product.getAvailabilities().add(availability);

            productService.create(product);
            products[i] = product;
            LOGGER.info("✓ Product created: {}", productData[i][1]);
        }
        return products;
    }

    private void createReviews(Product[] products, Customer[] customers, Language language) throws Exception {
        String[][] reviewData = {
            {"5.0", "Excellent product!", "This product exceeded my expectations. Highly recommended!"},
            {"4.5", "Very good quality", "Great value for money. Fast shipping and well packaged."},
            {"4.0", "Good purchase", "Satisfied with the product. Works as described."}
        };

        for (int i = 0; i < products.length; i++) {
            ProductReview review = new ProductReview();
            review.setProduct(products[i]);
            review.setCustomer(customers[i % customers.length]);
            review.setReviewRating(Double.parseDouble(reviewData[i][0]));
            review.setReviewDate(new Date());
            review.setReviewRead(0L);
            review.setStatus(1);

            ProductReviewDescription desc = new ProductReviewDescription();
            desc.setLanguage(language);
            desc.setName(reviewData[i][1]);
            desc.setDescription(reviewData[i][2]);
            desc.setProductReview(review);

            Set<ProductReviewDescription> descriptions = new HashSet<>();
            descriptions.add(desc);
            review.setDescriptions(descriptions);

            productReviewService.create(review);
            LOGGER.info("✓ Review created for product: {}", products[i].getSku());
        }
    }
}
