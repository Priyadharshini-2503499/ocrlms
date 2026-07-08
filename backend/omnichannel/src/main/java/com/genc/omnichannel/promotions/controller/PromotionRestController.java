package com.genc.omnichannel.promotions.controller;

import com.genc.omnichannel.promotions.dto.ApplyCouponRequestDTO;
import com.genc.omnichannel.promotions.dto.CouponRequestDTO;
import com.genc.omnichannel.promotions.dto.CouponResponseDTO;
import com.genc.omnichannel.promotions.exception.CouponException;
import com.genc.omnichannel.promotions.model.Coupon;
import com.genc.omnichannel.promotions.service.PromotionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for the Promotions (Coupon Management) module.
 *
 * Authorization rules:
 *  - GET endpoints: ADMIN, MARKETING_MANAGER (enforced by SecurityConfig)
 *  - CREATE, UPDATE, DELETE: ADMIN, MARKETING_MANAGER only (@PreAuthorize)
 */
@RestController
@RequestMapping("/api/promotions")
public class PromotionRestController {

    private final PromotionService service;

    public PromotionRestController(PromotionService service) {
        this.service = service;
    }

    // ---- READ -------------------------------------------------------

    /** Returns all coupons regardless of status. */
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(service.getAllCoupons());
    }

    /** Returns a single coupon by ID. */
    @GetMapping("/coupons/{id}")
    public ResponseEntity<Map<String, Object>> getCouponById(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Coupon coupon = service.getCouponById(id);
            response.put("success", true);
            response.put("coupon", coupon);
            return ResponseEntity.ok(response);
        } catch (CouponException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Returns active coupons applicable to a loyalty tier.
     * Includes both tier-specific coupons and GLOBAL coupons.
     * Public — used by the apply-coupon flow without authentication.
     */
    @GetMapping("/coupons/tier/{tierName}")
    public ResponseEntity<List<Coupon>> getCouponsByTier(@PathVariable String tierName) {
        return ResponseEntity.ok(service.getCouponsByTier(tierName));
    }

    // ---- CREATE -----------------------------------------------------

    /**
     * Creates a new coupon.
     * Restricted to ADMIN and MARKETING_MANAGER.
     */
    @PostMapping("/coupons")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<Map<String, Object>> createCoupon(@RequestBody CouponRequestDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            service.createPromotion(dto);
            response.put("success", true);
            response.put("message", "Coupon created successfully.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CouponException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to create coupon.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ---- UPDATE -----------------------------------------------------

    /**
     * Updates all fields of an existing coupon.
     * Restricted to ADMIN and MARKETING_MANAGER.
     */
    @PutMapping("/coupons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<Map<String, Object>> updateCoupon(
            @PathVariable int id,
            @RequestBody CouponRequestDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            service.updateCoupon(id, dto);
            response.put("success", true);
            response.put("message", "Coupon updated successfully.");
            return ResponseEntity.ok(response);
        } catch (CouponException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to update coupon.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ---- DELETE -----------------------------------------------------

    /**
     * Permanently deletes a coupon.
     * Restricted to ADMIN and MARKETING_MANAGER.
     */
    @DeleteMapping("/coupons/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ResponseEntity<Map<String, Object>> deleteCoupon(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            service.deleteCoupon(id);
            response.put("success", true);
            response.put("message", "Coupon deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (CouponException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete coupon.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ---- APPLY ------------------------------------------------------

    /**
     * Validates a coupon and returns the discounted total.
     * Accepts an optional customerId to enable loyalty-tier and first-time checks.
     *
     * Request body (JSON):
     * {
     *   "code": "GOLD20",
     *   "amount": 2500.00,
     *   "customerId": 2          // optional
     * }
     */
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyCoupon(@RequestBody ApplyCouponRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            CouponResponseDTO result = service.applyDiscount(
                    request.getCode(),
                    request.getCustomerId(),
                    request.getAmount());
            response.put("success", true);
            response.put("message", result.getMessage());
            response.put("finalAmount", result.getFinalAmount());
            response.put("couponCode", result.getCouponCode());
            return ResponseEntity.ok(response);
        } catch (CouponException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to apply coupon.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
