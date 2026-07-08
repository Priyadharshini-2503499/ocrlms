package com.genc.omnichannel.promotions.dto;

import com.genc.omnichannel.promotions.model.Coupon;

import java.time.LocalDate;

/**
 * Inbound DTO for creating or updating a coupon.
 * All fields except couponStatus are applicable on both create and update.
 */
public class CouponRequestDTO {

    private String couponCode;
    private String discountType;
    private double discountValue;
    private LocalDate validFrom;
    private LocalDate validTo;

    /** GLOBAL | SILVER | GOLD | PLATINUM — defaults to GLOBAL if blank. */
    private String targetTier;

    /** Minimum cart total required to apply this coupon (0 = no minimum). */
    private double minimumBasketValue = 0.0;

    /** Maximum number of global redemptions allowed (0 = unlimited). */
    private int maxRedemptionCount = 0;

    /**
     * Optional: override coupon status on update (ACTIVE / INACTIVE / EXPIRED / REDEEMED).
     * Ignored during create (always set to ACTIVE).
     */
    private Coupon.CouponStatus couponStatus;

    // ---- Getters & Setters ----

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

    public String getTargetTier() { return targetTier; }
    public void setTargetTier(String targetTier) { this.targetTier = targetTier; }

    public double getMinimumBasketValue() { return minimumBasketValue; }
    public void setMinimumBasketValue(double minimumBasketValue) { this.minimumBasketValue = minimumBasketValue; }

    public int getMaxRedemptionCount() { return maxRedemptionCount; }
    public void setMaxRedemptionCount(int maxRedemptionCount) { this.maxRedemptionCount = maxRedemptionCount; }

    public Coupon.CouponStatus getCouponStatus() { return couponStatus; }
    public void setCouponStatus(Coupon.CouponStatus couponStatus) { this.couponStatus = couponStatus; }
}
