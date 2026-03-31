package com.salesmanager.shop.model.catalog.product;

import java.io.Serializable;

import com.salesmanager.shop.model.customer.ReadableCustomer;


public class ReadableProductReview extends ProductReviewEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	private ReadableCustomer customer;
	private String productName;
	private ReadableProductReviewReply reply;

	public ReadableCustomer getCustomer() {
		return customer;
	}

	public void setCustomer(ReadableCustomer customer) {
		this.customer = customer;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public ReadableProductReviewReply getReply() {
		return reply;
	}

	public void setReply(ReadableProductReviewReply reply) {
		this.reply = reply;
	}

}
