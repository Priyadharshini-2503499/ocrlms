package com.genc.omnichannel.promotions.controller;

import com.genc.omnichannel.promotions.dto.CouponRequestDTO;
import com.genc.omnichannel.promotions.dto.CouponResponseDTO;
import com.genc.omnichannel.promotions.model.Coupon;
import com.genc.omnichannel.promotions.service.PromotionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
public class PromotionRestController {

    private final PromotionService service;

    public PromotionRestController(PromotionService service) {
        this.service = service;
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(service.getAllCoupons());
    }

    @PostMapping("/coupons")
    public ResponseEntity<Map<String, Object>> createCoupon(@RequestBody CouponRequestDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            service.createPromotion(dto);
            response.put("success", true);
            response.put("message", "Coupon created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to create coupon");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyCoupon(@RequestParam String code,
                                                           @RequestParam double amount) {
        Map<String, Object> response = new HashMap<>();
        try {
            CouponResponseDTO result = service.applyDiscount(code, amount);
            response.put("success", true);
            response.put("message", result.getMessage());
            response.put("finalAmount", result.getFinalAmount());
            response.put("couponCode", result.getCouponCode());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "Failed to apply coupon");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @GetMapping("/coupons/tier/{tierName}")
    public ResponseEntity<List<Coupon>> getCouponsByTier(@PathVariable String tierName) {
        return ResponseEntity.ok(service.getCouponsByTier(tierName));
    }
}

