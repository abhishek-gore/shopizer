package com.salesmanager.shop.populator.catalog;

import java.util.Date;
import org.apache.commons.lang3.Validate;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.utils.AbstractDataPopulator;
import com.salesmanager.core.model.catalog.product.review.ProductReviewReply;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.product.PersistableProductReviewReply;

public class PersistableProductReviewReplyPopulator extends
    AbstractDataPopulator<PersistableProductReviewReply, ProductReviewReply> {

  @Override
  public ProductReviewReply populate(PersistableProductReviewReply source, ProductReviewReply target,
      MerchantStore store, Language language) throws ConversionException {
    Validate.notNull(source, "PersistableProductReviewReply cannot be null");

    if (target == null) {
      target = new ProductReviewReply();
    }

    target.setComment(source.getComment());
    target.setReplyDate(new Date());

    return target;
  }

  @Override
  protected ProductReviewReply createTarget() {
    return null;
  }
}
