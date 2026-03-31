package com.salesmanager.core.business.services.catalog.product.review;

import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.catalog.product.review.ProductReviewReply;

public interface ProductReviewReplyService extends SalesManagerEntityService<Long, ProductReviewReply> {
  ProductReviewReply getByReviewId(Long reviewId);
}
