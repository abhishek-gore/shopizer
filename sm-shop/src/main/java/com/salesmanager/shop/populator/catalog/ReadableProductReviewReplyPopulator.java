package com.salesmanager.shop.populator.catalog;

import org.apache.commons.lang3.Validate;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.utils.AbstractDataPopulator;
import com.salesmanager.core.model.catalog.product.review.ProductReviewReply;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.product.ReadableProductReviewReply;
import com.salesmanager.shop.utils.DateUtil;

public class ReadableProductReviewReplyPopulator extends
    AbstractDataPopulator<ProductReviewReply, ReadableProductReviewReply> {

  @Override
  public ReadableProductReviewReply populate(ProductReviewReply source, ReadableProductReviewReply target,
      MerchantStore store, Language language) throws ConversionException {
    Validate.notNull(source, "ProductReviewReply cannot be null");

    if (target == null) {
      target = new ReadableProductReviewReply();
    }

    target.setId(source.getId());
    target.setComment(source.getComment());
    target.setMerchantName(source.getMerchantName());
    
    if (source.getReplyDate() != null) {
      target.setDate(DateUtil.formatDate(source.getReplyDate()));
    }

    return target;
  }

  @Override
  protected ReadableProductReviewReply createTarget() {
    return null;
  }
}
