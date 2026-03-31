package com.salesmanager.core.model.catalog.product.review;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;

@Entity
@Table(name = "PRODUCT_REVIEW_REPLY")
public class ProductReviewReply extends SalesManagerEntity<Long, ProductReviewReply> implements Auditable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "PRODUCT_REVIEW_REPLY_ID", unique = true, nullable = false)
  @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", 
      valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_REVIEW_REPLY_SEQ_NEXT_VAL")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
  private Long id;

  @OneToOne
  @JoinColumn(name = "PRODUCT_REVIEW_ID", nullable = false)
  private ProductReview productReview;

  @Column(name = "COMMENT", length = 1000, nullable = false)
  private String comment;

  @Column(name = "MERCHANT_NAME", length = 100)
  private String merchantName;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "REPLY_DATE")
  private Date replyDate;

  @Embedded
  private AuditSection auditSection = new AuditSection();

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  public ProductReview getProductReview() {
    return productReview;
  }

  public void setProductReview(ProductReview productReview) {
    this.productReview = productReview;
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

  public Date getReplyDate() {
    return replyDate;
  }

  public void setReplyDate(Date replyDate) {
    this.replyDate = replyDate;
  }

  @Override
  public AuditSection getAuditSection() {
    return auditSection;
  }

  @Override
  public void setAuditSection(AuditSection auditSection) {
    this.auditSection = auditSection;
  }
}
