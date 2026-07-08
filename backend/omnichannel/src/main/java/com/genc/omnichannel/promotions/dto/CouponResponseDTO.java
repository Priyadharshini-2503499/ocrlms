package com.genc.omnichannel.promotions.dto;


public class CouponResponseDTO {

    private String couponCode;
    private String message;
    private double finalAmount;

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
}
