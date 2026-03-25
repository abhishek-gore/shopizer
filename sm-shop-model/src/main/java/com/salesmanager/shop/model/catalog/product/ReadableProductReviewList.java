package com.salesmanager.shop.model.catalog.product;

import java.io.Serializable;
import java.util.List;

public class ReadableProductReviewList implements Serializable {
  private static final long serialVersionUID = 1L;

  private List<ReadableProductReview> reviews;
  private long total;

  public List<ReadableProductReview> getReviews() {
    return reviews;
  }

  public void setReviews(List<ReadableProductReview> reviews) {
    this.reviews = reviews;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }
}
