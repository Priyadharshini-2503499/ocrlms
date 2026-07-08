package com.genc.omnichannel.promotions.model;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int couponId;

    @Column(name = "couponCode")
    private String couponCode;

    @Column(name = "discountType")
    private String discountType;

    @Column(name = "discountValue")
    private double discountValue;

    @Column(name = "validFrom")
    private LocalDate validFrom;

    @Column(name = "validTo")
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "couponStatus")
    private CouponStatus couponStatus;

    @Column(name = "targetTier", length = 50)
    private String targetTier;

    public enum CouponStatus {
        ACTIVE, EXPIRED, REDEEMED
    }

    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public CouponStatus getCouponStatus() { return couponStatus; }
    public void setCouponStatus(CouponStatus couponStatus) { this.couponStatus = couponStatus; }

    public String getTargetTier() { return targetTier; }
    public void setTargetTier(String targetTier) { this.targetTier = targetTier; }
}