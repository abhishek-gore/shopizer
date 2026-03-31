package com.salesmanager.core.business.services.catalog.product.review;

import javax.inject.Inject;
import org.springframework.stereotype.Service;
import com.salesmanager.core.business.repositories.catalog.product.review.ProductReviewReplyRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.catalog.product.review.ProductReviewReply;

@Service("productReviewReplyService")
public class ProductReviewReplyServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductReviewReply>
    implements ProductReviewReplyService {

  private ProductReviewReplyRepository productReviewReplyRepository;

  @Inject
  public ProductReviewReplyServiceImpl(ProductReviewReplyRepository productReviewReplyRepository) {
    super(productReviewReplyRepository);
    this.productReviewReplyRepository = productReviewReplyRepository;
  }

  @Override
  public ProductReviewReply getByReviewId(Long reviewId) {
    return productReviewReplyRepository.findByProductReviewId(reviewId).orElse(null);
  }
}
