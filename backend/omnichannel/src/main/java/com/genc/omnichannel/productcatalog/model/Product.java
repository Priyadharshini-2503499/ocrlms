package com.genc.omnichannel.productcatalog.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productId")
    private Long productId;

    @Column(name = "skuCode", nullable = false, unique = true, length = 50)
    private String skuCode;

    @Column(name = "productName", nullable = false, length = 150)
    private String productName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "basePrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "productStatus", nullable = false, length = 20)
    private ProductStatus productStatus = ProductStatus.ACTIVE;

    public Product() {
    }

    public Product(String skuCode, String productName, String category,
                   BigDecimal basePrice, ProductStatus productStatus) {
        this.skuCode = skuCode;
        this.productName = productName;
        this.category = category;
        this.basePrice = basePrice;
        this.productStatus = productStatus;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(ProductStatus productStatus) {
        this.productStatus = productStatus;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", skuCode='" + skuCode + '\'' +
                ", productName='" + productName + '\'' +
                ", category='" + category + '\'' +
                ", basePrice=" + basePrice +
                ", productStatus=" + productStatus +
                '}';
    }
}
