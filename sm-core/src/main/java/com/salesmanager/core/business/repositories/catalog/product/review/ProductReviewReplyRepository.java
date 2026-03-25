package com.salesmanager.core.business.repositories.catalog.product.review;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.salesmanager.core.model.catalog.product.review.ProductReviewReply;

public interface ProductReviewReplyRepository extends JpaRepository<ProductReviewReply, Long> {
  Optional<ProductReviewReply> findByProductReviewId(Long reviewId);
}
