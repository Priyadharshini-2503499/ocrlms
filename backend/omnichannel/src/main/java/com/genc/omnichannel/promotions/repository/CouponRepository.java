package com.genc.omnichannel.promotions.repository;

import com.genc.omnichannel.promotions.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    Optional<Coupon> findByCouponCode(String couponCode);
    List<Coupon> findByCouponStatusAndTargetTierIn(Coupon.CouponStatus status, List<String> targetTiers);}
