package com.genc.omnichannel.promotions.dto;

/**
 * Inbound DTO for the POST /api/promotions/apply endpoint.
 * customerId is optional — when absent, loyalty-tier and first-time
 * customer checks are skipped (useful for pure discount-math simulation).
 */
public class ApplyCouponRequestDTO {

    private String code;
    private double amount;

    /**
     * Optional customer ID.
     * - Enables loyalty-tier eligibility check.
     * - Enables WELCOME10 first-time-customer check.
     */
    private Long customerId;

    // ---- Getters & Setters ----

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
}
