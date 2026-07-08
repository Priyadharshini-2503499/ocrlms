package com.genc.omnichannel.promotions.model;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int couponId;

    @Column(name = "couponCode", unique = true, nullable = false, length = 50)
    private String couponCode;

    @Column(name = "discountType", nullable = false, length = 20)
    private String discountType;

    @Column(name = "discountValue", nullable = false)
    private double discountValue;

    @Column(name = "validFrom", nullable = false)
    private LocalDate validFrom;

    @Column(name = "validTo", nullable = false)
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "couponStatus", nullable = false)
    private CouponStatus couponStatus;

    /** GLOBAL means any tier. Otherwise one of SILVER, GOLD, PLATINUM. */
    @Column(name = "targetTier", length = 50)
    private String targetTier;

    /** Minimum cart/basket value required to use this coupon (0 = no minimum). */
    @Column(name = "minimumBasketValue", nullable = false)
    private double minimumBasketValue = 0.0;

    /** Maximum number of times this coupon can be redeemed across all customers (0 = unlimited). */
    @Column(name = "maxRedemptionCount", nullable = false)
    private int maxRedemptionCount = 0;

    /** Running count of how many times this coupon has been successfully redeemed. */
    @Column(name = "redemptionCount", nullable = false)
    private int redemptionCount = 0;

    public enum CouponStatus {
        ACTIVE, EXPIRED, REDEEMED, INACTIVE
    }

    // ---- Getters & Setters ----

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

    public double getMinimumBasketValue() { return minimumBasketValue; }
    public void setMinimumBasketValue(double minimumBasketValue) { this.minimumBasketValue = minimumBasketValue; }

    public int getMaxRedemptionCount() { return maxRedemptionCount; }
    public void setMaxRedemptionCount(int maxRedemptionCount) { this.maxRedemptionCount = maxRedemptionCount; }

    public int getRedemptionCount() { return redemptionCount; }
    public void setRedemptionCount(int redemptionCount) { this.redemptionCount = redemptionCount; }
}