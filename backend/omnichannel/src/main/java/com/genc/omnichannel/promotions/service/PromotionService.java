package com.genc.omnichannel.promotions.service;

import com.genc.omnichannel.loyalty.model.Customer;
import com.genc.omnichannel.loyalty.repository.CustomerRepository;
import com.genc.omnichannel.order.model.OrderStatus;
import com.genc.omnichannel.order.repository.OrderRepository;
import com.genc.omnichannel.promotions.dto.CouponRequestDTO;
import com.genc.omnichannel.promotions.dto.CouponResponseDTO;
import com.genc.omnichannel.promotions.exception.CouponException;
import com.genc.omnichannel.promotions.model.Coupon;
import com.genc.omnichannel.promotions.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionService {

    @Autowired
    private CouponRepository repo;

    // Cross-module access — same monolith, no Feign needed
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    // =========================================================
    //  CREATE
    // =========================================================

    @Transactional
    public void createPromotion(CouponRequestDTO dto) {
        // Duplicate code check (case-insensitive)
        if (repo.findByCouponCodeIgnoreCase(dto.getCouponCode().trim()).isPresent()) {
            throw new CouponException("A coupon with code '" + dto.getCouponCode() + "' already exists.");
        }

        if (dto.getValidFrom() != null && dto.getValidTo() != null
                && dto.getValidTo().isBefore(dto.getValidFrom())) {
            throw new CouponException("'Valid To' date must be on or after 'Valid From' date.");
        }

        Coupon c = new Coupon();
        c.setCouponCode(dto.getCouponCode().trim().toUpperCase());
        c.setDiscountType(dto.getDiscountType().toUpperCase());
        c.setDiscountValue(dto.getDiscountValue());
        c.setValidFrom(dto.getValidFrom());
        c.setValidTo(dto.getValidTo());
        c.setTargetTier(resolveTargetTier(dto.getTargetTier()));
        c.setMinimumBasketValue(Math.max(0, dto.getMinimumBasketValue()));
        c.setMaxRedemptionCount(Math.max(0, dto.getMaxRedemptionCount()));
        c.setRedemptionCount(0);
        c.setCouponStatus(Coupon.CouponStatus.ACTIVE);
        repo.save(c);
    }

    // =========================================================
    //  READ
    // =========================================================

    public List<Coupon> getAllCoupons() {
        return repo.findAll();
    }

    public Coupon getCouponById(int id) {
        return repo.findById(id)
                .orElseThrow(() -> new CouponException("Coupon with ID " + id + " not found."));
    }

    public List<Coupon> getCouponsByTier(String tierName) {
        List<String> applicableTiers = List.of(tierName.toUpperCase(), "GLOBAL");
        return repo.findByCouponStatusAndTargetTierIn(Coupon.CouponStatus.ACTIVE, applicableTiers);
    }

    // =========================================================
    //  UPDATE
    // =========================================================

    @Transactional
    public void updateCoupon(int id, CouponRequestDTO dto) {
        Coupon c = repo.findById(id)
                .orElseThrow(() -> new CouponException("Coupon with ID " + id + " not found."));

        // If code is being changed, make sure the new code is not already taken
        String newCode = dto.getCouponCode() != null ? dto.getCouponCode().trim().toUpperCase() : c.getCouponCode();
        if (!newCode.equals(c.getCouponCode())) {
            repo.findByCouponCodeIgnoreCase(newCode).ifPresent(existing -> {
                if (existing.getCouponId() != id) {
                    throw new CouponException("A coupon with code '" + newCode + "' already exists.");
                }
            });
        }

        if (dto.getValidFrom() != null && dto.getValidTo() != null
                && dto.getValidTo().isBefore(dto.getValidFrom())) {
            throw new CouponException("'Valid To' date must be on or after 'Valid From' date.");
        }

        c.setCouponCode(newCode);
        if (dto.getDiscountType() != null) c.setDiscountType(dto.getDiscountType().toUpperCase());
        if (dto.getDiscountValue() > 0) c.setDiscountValue(dto.getDiscountValue());
        if (dto.getValidFrom() != null) c.setValidFrom(dto.getValidFrom());
        if (dto.getValidTo() != null) c.setValidTo(dto.getValidTo());
        c.setTargetTier(resolveTargetTier(dto.getTargetTier()));
        c.setMinimumBasketValue(Math.max(0, dto.getMinimumBasketValue()));
        c.setMaxRedemptionCount(Math.max(0, dto.getMaxRedemptionCount()));
        if (dto.getCouponStatus() != null) c.setCouponStatus(dto.getCouponStatus());
        repo.save(c);
    }

    // =========================================================
    //  DELETE
    // =========================================================

    @Transactional
    public void deleteCoupon(int id) {
        if (!repo.existsById(id)) {
            throw new CouponException("Coupon with ID " + id + " not found.");
        }
        repo.deleteById(id);
    }

    // =========================================================
    //  VALIDATE — full pipeline (called before apply/redeem)
    // =========================================================

    /**
     * Validates a coupon against all business rules.
     *
     * @param code         coupon code
     * @param customerId   optional — enables loyalty-tier and first-time checks when provided
     * @param basketAmount the customer's current cart/basket total
     * @return the valid {@link Coupon}
     * @throws CouponException on any validation failure
     */
    public Coupon validateCoupon(String code, Long customerId, double basketAmount) {

        // 1. Coupon must exist
        Coupon coupon = repo.findByCouponCode(code.trim().toUpperCase())
                .orElseThrow(() -> new CouponException("Invalid coupon code '" + code + "'."));

        LocalDate today = LocalDate.now();

        // 2. Validity window — not started yet
        if (today.isBefore(coupon.getValidFrom())) {
            throw new CouponException("This coupon is not valid yet. It becomes active from "
                    + coupon.getValidFrom() + ".");
        }

        // 3. Validity window — expired; auto-mark
        if (today.isAfter(coupon.getValidTo())) {
            coupon.setCouponStatus(Coupon.CouponStatus.EXPIRED);
            repo.save(coupon);
            throw new CouponException("This coupon expired on " + coupon.getValidTo() + ".");
        }

        // 4. Status check
        switch (coupon.getCouponStatus()) {
            case EXPIRED   -> throw new CouponException("This coupon has expired.");
            case REDEEMED  -> throw new CouponException("This coupon has already been fully redeemed.");
            case INACTIVE  -> throw new CouponException("This coupon is currently inactive.");
            case ACTIVE    -> { /* proceed */ }
        }

        // 5. Max redemption limit (0 = unlimited)
        if (coupon.getMaxRedemptionCount() > 0
                && coupon.getRedemptionCount() >= coupon.getMaxRedemptionCount()) {
            throw new CouponException("This coupon has reached its maximum redemption limit.");
        }

        // 6. Minimum basket value
        if (coupon.getMinimumBasketValue() > 0 && basketAmount < coupon.getMinimumBasketValue()) {
            throw new CouponException(
                    String.format("Minimum order value of ₹%.0f is required to use this coupon.",
                            coupon.getMinimumBasketValue()));
        }

        // 7. Loyalty-tier eligibility (skip for GLOBAL coupons and when no customerId provided)
        String targetTier = coupon.getTargetTier();
        if (customerId != null && targetTier != null && !targetTier.equalsIgnoreCase("GLOBAL")) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CouponException("Customer with ID " + customerId + " not found."));
            String customerTier = customer.getLoyaltyTier().name(); // SILVER / GOLD / PLATINUM
            if (!customerTier.equalsIgnoreCase(targetTier)) {
                throw new CouponException(
                        "This coupon is not available for your loyalty tier. "
                        + "This coupon is restricted to " + targetTier + " members.");
            }
        }

        // 8. WELCOME10 — first-time customer check
        if ("WELCOME10".equalsIgnoreCase(coupon.getCouponCode()) && customerId != null) {
            boolean hasExistingOrders = orderRepository
                    .existsByCustomer_CustomerIdAndOrderStatusNot(customerId, OrderStatus.CANCELLED);
            if (hasExistingOrders) {
                throw new CouponException("WELCOME10 is valid only for first-time customers.");
            }
        }

        return coupon;
    }

    // =========================================================
    //  APPLY DISCOUNT
    // =========================================================

    /**
     * Validates the coupon, computes the discounted total, increments the
     * redemption counter, and returns the result DTO.
     */
    @Transactional
    public CouponResponseDTO applyDiscount(String code, Long customerId, double amount) {

        Coupon coupon = validateCoupon(code, customerId, amount);

        double finalAmount;
        if (coupon.getDiscountType().equalsIgnoreCase("PERCENTAGE")) {
            finalAmount = amount - (amount * coupon.getDiscountValue() / 100);
        } else {
            finalAmount = amount - coupon.getDiscountValue();
        }
        if (finalAmount < 0) finalAmount = 0;

        // Increment redemption counter
        coupon.setRedemptionCount(coupon.getRedemptionCount() + 1);

        // Auto-mark as REDEEMED once the cap is hit
        if (coupon.getMaxRedemptionCount() > 0
                && coupon.getRedemptionCount() >= coupon.getMaxRedemptionCount()) {
            coupon.setCouponStatus(Coupon.CouponStatus.REDEEMED);
        }

        repo.save(coupon);

        CouponResponseDTO response = new CouponResponseDTO();
        response.setCouponCode(coupon.getCouponCode());
        response.setFinalAmount(finalAmount);
        response.setMessage("Discount applied successfully! You saved "
                + (coupon.getDiscountType().equalsIgnoreCase("PERCENTAGE")
                        ? coupon.getDiscountValue() + "%"
                        : "₹" + String.format("%.0f", coupon.getDiscountValue()))
                + " on your order.");
        return response;
    }

    // =========================================================
    //  MARK REDEEMED (kept for backward compatibility)
    // =========================================================

    @Transactional
    public void markCouponRedeemed(String code) {
        Coupon coupon = validateCoupon(code, null, 0);
        coupon.setCouponStatus(Coupon.CouponStatus.REDEEMED);
        repo.save(coupon);
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    private String resolveTargetTier(String tier) {
        if (tier == null || tier.trim().isEmpty()) return "GLOBAL";
        return tier.trim().toUpperCase();
    }
}
