package com.salesmanager.shop.model.catalog.product;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

public class PersistableProductReviewReply implements Serializable {
  private static final long serialVersionUID = 1L;

  @NotEmpty(message = "Comment is required")
  private String comment;

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
