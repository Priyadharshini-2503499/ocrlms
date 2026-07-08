package com.genc.omnichannel.promotions.exception;

/**
 * Domain exception thrown by the Promotions service for all business-rule
 * violations (expired coupon, tier mismatch, basket too low, etc.).
 * Controllers catch this and return HTTP 400/422 with the message.
 */
public class CouponException extends RuntimeException {

    public CouponException(String message) {
        super(message);
    }
}
