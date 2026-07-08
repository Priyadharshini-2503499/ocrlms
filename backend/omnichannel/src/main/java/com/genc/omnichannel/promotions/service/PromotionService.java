package com.genc.omnichannel.promotions.service;

import com.genc.omnichannel.promotions.dto.CouponRequestDTO;
import com.genc.omnichannel.promotions.dto.CouponResponseDTO;
import com.genc.omnichannel.promotions.model.Coupon;
import com.genc.omnichannel.promotions.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionService {

    @Autowired
    private CouponRepository repo;

    public void createPromotion(CouponRequestDTO dto) {
        Coupon c = new Coupon();
        c.setCouponCode(dto.getCouponCode());
        c.setDiscountType(dto.getDiscountType());
        c.setDiscountValue(dto.getDiscountValue());
        c.setValidFrom(dto.getValidFrom());
        c.setValidTo(dto.getValidTo());

        if (dto.getTargetTier() == null || dto.getTargetTier().trim().isEmpty()) {
            c.setTargetTier("GLOBAL");
        } else {
            c.setTargetTier(dto.getTargetTier().toUpperCase());
        }

        c.setCouponStatus(Coupon.CouponStatus.ACTIVE);
        repo.save(c);
    }
    public List<Coupon> getCouponsByTier(String tierName) {
        List<String> applicableTiers = List.of(tierName.toUpperCase(), "GLOBAL");
        return repo.findByCouponStatusAndTargetTierIn(Coupon.CouponStatus.ACTIVE, applicableTiers);
    }
    public List<Coupon> getAllCoupons() {
        return repo.findAll();
    }

    public Coupon validateCoupon(String code) {

        Coupon coupon = repo.findByCouponCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid Coupon"));

        LocalDate today = LocalDate.now();

        if (today.isAfter(coupon.getValidTo())) {
            coupon.setCouponStatus(Coupon.CouponStatus.EXPIRED);
            repo.save(coupon);
            throw new RuntimeException("Coupon Expired");
        }

        if (coupon.getCouponStatus() == Coupon.CouponStatus.REDEEMED) {
            throw new RuntimeException("Coupon Already Used");
        }

        if (coupon.getCouponStatus() != Coupon.CouponStatus.ACTIVE) {
            throw new RuntimeException("Coupon Not Active");
        }

        return coupon;
    }

    public CouponResponseDTO applyDiscount(String code, double amount) {

        Coupon coupon = validateCoupon(code);

        double finalAmount;

        if (coupon.getDiscountType().equalsIgnoreCase("PERCENTAGE")) {
            finalAmount = amount - (amount * coupon.getDiscountValue() / 100);
        } else {
            finalAmount = amount - coupon.getDiscountValue();
        }

        if (finalAmount < 0) {
            finalAmount = 0;
        }

        CouponResponseDTO response = new CouponResponseDTO();
        response.setCouponCode(code);
        response.setFinalAmount(finalAmount);
        response.setMessage("Discount Applied Successfully");

        return response;
    }

    public void markCouponRedeemed(String code) {

        Coupon coupon = validateCoupon(code);

        coupon.setCouponStatus(Coupon.CouponStatus.REDEEMED);

        repo.save(coupon);
    }
}
