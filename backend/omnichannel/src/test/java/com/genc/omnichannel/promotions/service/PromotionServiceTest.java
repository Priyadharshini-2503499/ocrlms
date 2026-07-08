package com.genc.omnichannel.promotions.service;

import com.genc.omnichannel.promotions.dto.CouponRequestDTO;
import com.genc.omnichannel.promotions.dto.CouponResponseDTO;
import com.genc.omnichannel.promotions.model.Coupon;
import com.genc.omnichannel.promotions.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceSimpleTest {

    @Mock
    private CouponRepository repo;

    @InjectMocks
    private PromotionService promotionService;

    @Test
    void testCreatePromotion() {
        CouponRequestDTO dto = new CouponRequestDTO();
        dto.setCouponCode("SAVE10");
        dto.setDiscountType("PERCENTAGE");
        dto.setDiscountValue(10.0);

        promotionService.createPromotion(dto);

        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        verify(repo, times(1)).save(captor.capture());

        assertEquals("SAVE10", captor.getValue().getCouponCode());
        assertEquals(Coupon.CouponStatus.ACTIVE, captor.getValue().getCouponStatus());
    }

    @Test
    void testApplyDiscount_Success() {
        Coupon activeCoupon = new Coupon();
        activeCoupon.setCouponCode("PERCENT20");
        activeCoupon.setDiscountType("PERCENTAGE");
        activeCoupon.setDiscountValue(20.0);
        activeCoupon.setValidTo(LocalDate.now().plusDays(5)); // Valid
        activeCoupon.setCouponStatus(Coupon.CouponStatus.ACTIVE);

        when(repo.findByCouponCode("PERCENT20")).thenReturn(Optional.of(activeCoupon));

        CouponResponseDTO response = promotionService.applyDiscount("PERCENT20", 100.0);

        assertNotNull(response);
        assertEquals(80.0, response.getFinalAmount());
    }
}
