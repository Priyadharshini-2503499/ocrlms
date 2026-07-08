package com.genc.omnichannel.productcatalog.dto;

import java.math.BigDecimal;

import com.genc.omnichannel.productcatalog.model.ProductStatus;

public class ProductResponse {

    private final Long productId;
    private final String skuCode;
    private final String productName;
    private final String category;
    private final BigDecimal basePrice;
    private final ProductStatus productStatus;

    public ProductResponse(Long productId, String skuCode, String productName,
                           String category, BigDecimal basePrice, ProductStatus productStatus) {
        this.productId = productId;
        this.skuCode = skuCode;
        this.productName = productName;
        this.category = category;
        this.basePrice = basePrice;
        this.productStatus = productStatus;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }
    public String getBadgeClass() {
        if (productStatus == null) {
            return "b-gray";
        }
        return switch (productStatus) {
            case ACTIVE -> "b-green";
            case DISCONTINUED -> "b-rust";
            case INACTIVE -> "b-gray";
        };
    }
}
