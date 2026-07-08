package com.genc.omnichannel.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class PlaceOrderRequest {

    @NotNull(message = "Customer Id is required")
    private Long customerId;

    @NotEmpty(message = "OrderChannel is required")
    @Pattern(regexp = "ONLINE|INSTORE|MOBILE",message = "Invalid Input provide it can be either ONLINE,INSTORE,MOBILE")
    private String orderChannel;

    @NotEmpty(message = "At least one order item is required")
    @Valid
    private List<OrderItemRequest> items = new ArrayList<>();

    public PlaceOrderRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getOrderChannel() {
        return orderChannel;
    }

    public void setOrderChannel(String orderChannel) {
        this.orderChannel = orderChannel;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "PlaceOrderRequest{" +
                "customerId=" + customerId +
                ", orderChannel='" + orderChannel + '\'' +
                ", items=" + items +
                '}';
    }
}
