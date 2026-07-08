package com.genc.omnichannel.promotions.service;

import com.genc.omnichannel.loyalty.model.Customer;
import com.genc.omnichannel.loyalty.model.LoyaltyTier;
import com.genc.omnichannel.loyalty.repository.CustomerRepository;
import com.genc.omnichannel.order.model.OrderStatus;
import com.genc.omnichannel.order.repository.OrderRepository;
import com.genc.omnichannel.promotions.dto.CouponRequestDTO;
import com.genc.omnichannel.promotions.dto.CouponResponseDTO;
import com.genc.omnichannel.promotions.exception.CouponException;
import com.genc.omnichannel.promotions.model.Coupon;
import com.genc.omnichannel.promotions.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PromotionService}.
 *
 * All repository dependencies are mocked — no database needed.
 * Tests verify every branch in the 8-step validation pipeline.
 */
@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock private CouponRepository couponRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private PromotionService promotionService;

    // ---- Shared test fixtures ----------------------------------------

    private Coupon activeCoupon(String code, String discountType, double discountValue) {
        Coupon c = new Coupon();
        c.setCouponId(1);
        c.setCouponCode(code);
        c.setDiscountType(discountType);
        c.setDiscountValue(discountValue);
        c.setValidFrom(LocalDate.now().minusDays(10));
        c.setValidTo(LocalDate.now().plusDays(30));
        c.setCouponStatus(Coupon.CouponStatus.ACTIVE);
        c.setTargetTier("GLOBAL");
        c.setMinimumBasketValue(0.0);
        c.setMaxRedemptionCount(0);
        c.setRedemptionCount(0);
        return c;
    }

    private Customer silverCustomer(Long id) {
        Customer cust = new Customer();
        cust.setCustomerId(id);
        cust.setLoyaltyTier(LoyaltyTier.SILVER);
        return cust;
    }

    private Customer goldCustomer(Long id) {
        Customer cust = new Customer();
        cust.setCustomerId(id);
        cust.setLoyaltyTier(LoyaltyTier.GOLD);
        return cust;
    }

    // ================================================================
    //  validateCoupon — existence
    // ================================================================

    @Test
    @DisplayName("Invalid coupon code throws CouponException")
    void invalidCode_throwsException() {
        when(couponRepository.findByCouponCode("FAKECODE")).thenReturn(Optional.empty());

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("FAKECODE", null, 100.0));
        assertTrue(ex.getMessage().contains("Invalid coupon code"));
    }

    // ================================================================
    //  validateCoupon — validity window
    // ================================================================

    @Test
    @DisplayName("Coupon not yet valid (future validFrom) throws CouponException")
    void futureStart_throwsException() {
        Coupon c = activeCoupon("FUTURE10", "PERCENTAGE", 10);
        c.setValidFrom(LocalDate.now().plusDays(5));
        c.setValidTo(LocalDate.now().plusDays(35));

        when(couponRepository.findByCouponCode("FUTURE10")).thenReturn(Optional.of(c));

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("FUTURE10", null, 100.0));
        assertTrue(ex.getMessage().contains("not valid yet"));
    }

    @Test
    @DisplayName("Expired coupon (past validTo) throws CouponException and auto-marks EXPIRED")
    void expiredByDate_throwsAndMarksExpired() {
        Coupon c = activeCoupon("OLD10", "PERCENTAGE", 10);
        c.setValidTo(LocalDate.now().minusDays(1));

        when(couponRepository.findByCouponCode("OLD10")).thenReturn(Optional.of(c));
        when(couponRepository.save(c)).thenReturn(c);

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("OLD10", null, 100.0));
        assertTrue(ex.getMessage().contains("expired on"));
        assertEquals(Coupon.CouponStatus.EXPIRED, c.getCouponStatus());
        verify(couponRepository).save(c);
    }

    // ================================================================
    //  validateCoupon — status
    // ================================================================

    @Test
    @DisplayName("REDEEMED coupon throws CouponException")
    void redeemedStatus_throwsException() {
        Coupon c = activeCoupon("USED", "FLAT", 200);
        c.setCouponStatus(Coupon.CouponStatus.REDEEMED);

        when(couponRepository.findByCouponCode("USED")).thenReturn(Optional.of(c));

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("USED", null, 500.0));
        assertTrue(ex.getMessage().contains("already been fully redeemed"));
    }

    @Test
    @DisplayName("INACTIVE coupon throws CouponException")
    void inactiveStatus_throwsException() {
        Coupon c = activeCoupon("PAUSED", "PERCENTAGE", 5);
        c.setCouponStatus(Coupon.CouponStatus.INACTIVE);

        when(couponRepository.findByCouponCode("PAUSED")).thenReturn(Optional.of(c));

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("PAUSED", null, 500.0));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    // ================================================================
    //  validateCoupon — max redemption cap
    // ================================================================

    @Test
    @DisplayName("Max redemption limit exceeded throws CouponException")
    void maxRedemptionExceeded_throwsException() {
        Coupon c = activeCoupon("CAP5", "PERCENTAGE", 10);
        c.setMaxRedemptionCount(5);
        c.setRedemptionCount(5);

        when(couponRepository.findByCouponCode("CAP5")).thenReturn(Optional.of(c));

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("CAP5", null, 500.0));
        assertTrue(ex.getMessage().contains("maximum redemption limit"));
    }

    // ================================================================
    //  validateCoupon — minimum basket value
    // ================================================================

    @Test
    @DisplayName("Basket below minimum throws CouponException with amount in message")
    void basketBelowMinimum_throwsException() {
        Coupon c = activeCoupon("FLAT500", "FLAT", 500);
        c.setMinimumBasketValue(1000.0);

        when(couponRepository.findByCouponCode("FLAT500")).thenReturn(Optional.of(c));

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("FLAT500", null, 800.0));
        assertTrue(ex.getMessage().contains("1000"));
    }

    @Test
    @DisplayName("Basket exactly at minimum is accepted")
    void basketExactlyAtMinimum_accepted() {
        Coupon c = activeCoupon("FLAT500", "FLAT", 500);
        c.setMinimumBasketValue(1000.0);

        when(couponRepository.findByCouponCode("FLAT500")).thenReturn(Optional.of(c));

        assertDoesNotThrow(() -> promotionService.validateCoupon("FLAT500", null, 1000.0));
    }

    // ================================================================
    //  validateCoupon — loyalty tier
    // ================================================================

    @Test
    @DisplayName("GOLD coupon applied by SILVER customer throws CouponException")
    void wrongTier_throwsException() {
        Coupon c = activeCoupon("GOLD20", "PERCENTAGE", 20);
        c.setTargetTier("GOLD");

        when(couponRepository.findByCouponCode("GOLD20")).thenReturn(Optional.of(c));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(silverCustomer(1L)));

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("GOLD20", 1L, 1000.0));
        assertTrue(ex.getMessage().contains("not available for your loyalty tier"));
        assertTrue(ex.getMessage().contains("GOLD"));
    }

    @Test
    @DisplayName("GOLD coupon applied by GOLD customer is accepted")
    void correctTier_accepted() {
        Coupon c = activeCoupon("GOLD20", "PERCENTAGE", 20);
        c.setTargetTier("GOLD");

        when(couponRepository.findByCouponCode("GOLD20")).thenReturn(Optional.of(c));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(goldCustomer(2L)));

        assertDoesNotThrow(() -> promotionService.validateCoupon("GOLD20", 2L, 1000.0));
    }

    @Test
    @DisplayName("GLOBAL coupon is accepted by any loyalty tier without customer lookup")
    void globalCoupon_acceptedByAnyTier() {
        Coupon c = activeCoupon("FLAT500", "FLAT", 500);
        c.setTargetTier("GLOBAL");

        when(couponRepository.findByCouponCode("FLAT500")).thenReturn(Optional.of(c));

        assertDoesNotThrow(() -> promotionService.validateCoupon("FLAT500", 1L, 1000.0));
        verifyNoInteractions(customerRepository);
    }

    // ================================================================
    //  validateCoupon — WELCOME10 first-time customer check
    // ================================================================

    @Test
    @DisplayName("WELCOME10 rejected for returning customer")
    void welcome10_returningCustomer_throwsException() {
        Coupon c = activeCoupon("WELCOME10", "PERCENTAGE", 10);
        c.setTargetTier("GLOBAL");

        when(couponRepository.findByCouponCode("WELCOME10")).thenReturn(Optional.of(c));
        when(orderRepository.existsByCustomer_CustomerIdAndOrderStatusNot(5L, OrderStatus.CANCELLED))
                .thenReturn(true);

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.validateCoupon("WELCOME10", 5L, 500.0));
        assertTrue(ex.getMessage().contains("WELCOME10 is valid only for first-time customers"));
    }

    @Test
    @DisplayName("WELCOME10 accepted for first-time customer with no prior orders")
    void welcome10_firstTimeCustomer_accepted() {
        Coupon c = activeCoupon("WELCOME10", "PERCENTAGE", 10);
        c.setTargetTier("GLOBAL");

        when(couponRepository.findByCouponCode("WELCOME10")).thenReturn(Optional.of(c));
        when(orderRepository.existsByCustomer_CustomerIdAndOrderStatusNot(99L, OrderStatus.CANCELLED))
                .thenReturn(false);

        assertDoesNotThrow(() -> promotionService.validateCoupon("WELCOME10", 99L, 500.0));
    }

    @Test
    @DisplayName("WELCOME10 without customerId skips first-time check")
    void welcome10_noCustomerId_skipsCheck() {
        Coupon c = activeCoupon("WELCOME10", "PERCENTAGE", 10);
        c.setTargetTier("GLOBAL");

        when(couponRepository.findByCouponCode("WELCOME10")).thenReturn(Optional.of(c));

        assertDoesNotThrow(() -> promotionService.validateCoupon("WELCOME10", null, 500.0));
        verifyNoInteractions(orderRepository);
    }

    // ================================================================
    //  applyDiscount — discount computation
    // ================================================================

    @Test
    @DisplayName("PERCENTAGE discount computed correctly")
    void applyDiscount_percentageType() {
        Coupon c = activeCoupon("SUMMER20", "PERCENTAGE", 20);
        when(couponRepository.findByCouponCode("SUMMER20")).thenReturn(Optional.of(c));
        when(couponRepository.save(any())).thenReturn(c);

        CouponResponseDTO result = promotionService.applyDiscount("SUMMER20", null, 1000.0);

        assertEquals(800.0, result.getFinalAmount(), 0.01);
        assertTrue(result.getMessage().contains("20%"));
        assertEquals("SUMMER20", result.getCouponCode());
    }

    @Test
    @DisplayName("FLAT discount computed correctly")
    void applyDiscount_flatType() {
        Coupon c = activeCoupon("FLAT200", "FLAT", 200);
        when(couponRepository.findByCouponCode("FLAT200")).thenReturn(Optional.of(c));
        when(couponRepository.save(any())).thenReturn(c);

        CouponResponseDTO result = promotionService.applyDiscount("FLAT200", null, 800.0);

        assertEquals(600.0, result.getFinalAmount(), 0.01);
    }

    @Test
    @DisplayName("FLAT discount that would go negative is clamped to zero")
    void applyDiscount_negativeClampedToZero() {
        Coupon c = activeCoupon("MEGA1000", "FLAT", 1000);
        when(couponRepository.findByCouponCode("MEGA1000")).thenReturn(Optional.of(c));
        when(couponRepository.save(any())).thenReturn(c);

        CouponResponseDTO result = promotionService.applyDiscount("MEGA1000", null, 200.0);

        assertEquals(0.0, result.getFinalAmount(), 0.01);
    }

    @Test
    @DisplayName("Redemption counter increments and coupon auto-marked REDEEMED when cap reached")
    void applyDiscount_incrementsAndAutoMarksRedeemed() {
        Coupon c = activeCoupon("ONCE5", "PERCENTAGE", 5);
        c.setMaxRedemptionCount(1);
        c.setRedemptionCount(0);

        when(couponRepository.findByCouponCode("ONCE5")).thenReturn(Optional.of(c));
        when(couponRepository.save(any())).thenReturn(c);

        promotionService.applyDiscount("ONCE5", null, 500.0);

        assertEquals(1, c.getRedemptionCount());
        assertEquals(Coupon.CouponStatus.REDEEMED, c.getCouponStatus());
        verify(couponRepository).save(c);
    }

    // ================================================================
    //  createPromotion — duplicate code guard
    // ================================================================

    @Test
    @DisplayName("Creating a coupon with duplicate code throws CouponException")
    void createPromotion_duplicateCode_throwsException() {
        CouponRequestDTO dto = new CouponRequestDTO();
        dto.setCouponCode("FLAT500");
        dto.setDiscountType("FLAT");
        dto.setDiscountValue(500);
        dto.setValidFrom(LocalDate.now());
        dto.setValidTo(LocalDate.now().plusDays(30));

        when(couponRepository.findByCouponCodeIgnoreCase("FLAT500"))
                .thenReturn(Optional.of(activeCoupon("FLAT500", "FLAT", 500)));

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.createPromotion(dto));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    // ================================================================
    //  deleteCoupon
    // ================================================================

    @Test
    @DisplayName("Delete non-existent coupon throws CouponException")
    void deleteCoupon_notFound_throwsException() {
        when(couponRepository.existsById(999)).thenReturn(false);

        CouponException ex = assertThrows(CouponException.class,
                () -> promotionService.deleteCoupon(999));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Delete existing coupon calls repository deleteById")
    void deleteCoupon_callsDelete() {
        when(couponRepository.existsById(1)).thenReturn(true);
        doNothing().when(couponRepository).deleteById(1);

        assertDoesNotThrow(() -> promotionService.deleteCoupon(1));
        verify(couponRepository).deleteById(1);
    }
}
