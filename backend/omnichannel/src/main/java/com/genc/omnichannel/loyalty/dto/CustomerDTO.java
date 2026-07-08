package com.genc.omnichannel.loyalty.dto;

import com.genc.omnichannel.loyalty.model.LoyaltyTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CustomerDTO {

    private Long customerId;

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters.")
    private String fullName;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please provide a valid email address.")
    private String emailId;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\+?[\\d\\s\\-\\(\\)]{7,20}$", message = "Phone number must be 7–20 characters and may include digits, spaces, +, -, or parentheses.")
    private String phoneNumber;

    private Integer loyaltyPoints;
    private LoyaltyTier loyaltyTier;


    public CustomerDTO() {}


    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Integer getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(Integer loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public LoyaltyTier getLoyaltyTier() { return loyaltyTier; }
    public void setLoyaltyTier(LoyaltyTier loyaltyTier) { this.loyaltyTier = loyaltyTier; }
}