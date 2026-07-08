package com.genc.omnichannel.promotions.repository;

import com.genc.omnichannel.promotions.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    /** Case-sensitive lookup — used for apply/validate. */
    Optional<Coupon> findByCouponCode(String couponCode);

    /** Case-insensitive lookup — used when checking for duplicate codes on create. */
    Optional<Coupon> findByCouponCodeIgnoreCase(String couponCode);

    /** Returns active coupons matching any of the provided target tiers (e.g. ["GOLD","GLOBAL"]). */
    List<Coupon> findByCouponStatusAndTargetTierIn(Coupon.CouponStatus status, List<String> targetTiers);
}
