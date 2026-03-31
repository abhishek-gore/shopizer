package com.salesmanager.shop.model.catalog.product;

import java.io.Serializable;

public class ReadableProductReviewReply implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private String comment;
  private String merchantName;
  private String date;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public void setMerchantName(String merchantName) {
    this.merchantName = merchantName;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }
}
