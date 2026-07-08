package com.genc.omnichannel.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long orderId){
        super("Order not found with ID: "+orderId);
    }
}
