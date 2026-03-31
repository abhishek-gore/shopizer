package com.salesmanager.shop.populator.catalog;

import java.util.Set;

import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.utils.AbstractDataPopulator;
import com.salesmanager.core.model.catalog.product.review.ProductReview;
import com.salesmanager.core.model.catalog.product.review.ProductReviewDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.product.ReadableProductReview;
import com.salesmanager.shop.model.catalog.product.ReadableProductReviewReply;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.populator.customer.ReadableCustomerPopulator;
import com.salesmanager.shop.utils.DateUtil;

public class ReadableProductReviewPopulator extends
		AbstractDataPopulator<ProductReview, ReadableProductReview> {

	@Override
	public ReadableProductReview populate(ProductReview source,
			ReadableProductReview target, MerchantStore store, Language language)
			throws ConversionException {

		
		try {
			ReadableCustomerPopulator populator = new ReadableCustomerPopulator();
			ReadableCustomer customer = new ReadableCustomer();
			populator.populate(source.getCustomer(), customer, store, language);

			target.setId(source.getId());
			target.setDate(DateUtil.formatDate(source.getReviewDate()));
			target.setCustomer(customer);
			target.setRating(source.getReviewRating());
			target.setProductId(source.getProduct().getId());
			target.setProductName(source.getProduct().getDescriptions().iterator().next().getName());
			
			Set<ProductReviewDescription> descriptions = source.getDescriptions();
			if(descriptions!=null) {
				for(ProductReviewDescription description : descriptions) {
					target.setDescription(description.getDescription());
					target.setLanguage(description.getLanguage().getCode());
					break;
				}
			}

			// Populate reply if exists
			if (source.getReply() != null) {
				ReadableProductReviewReplyPopulator replyPopulator = new ReadableProductReviewReplyPopulator();
				ReadableProductReviewReply reply = replyPopulator.populate(source.getReply(), 
						new ReadableProductReviewReply(), store, language);
				target.setReply(reply);
			}

			return target;
			
		} catch (Exception e) {
			throw new ConversionException("Cannot populate ProductReview", e);
		}
		
		
		
	}

	@Override
	protected ReadableProductReview createTarget() {
		return null;
	}

}
