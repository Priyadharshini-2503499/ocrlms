package com.genc.omnichannel.order.mapper;

import com.genc.omnichannel.order.dto.OrderItemResponse;
import com.genc.omnichannel.order.dto.OrderResponse;
import com.genc.omnichannel.order.dto.PlaceOrderRequest;
import com.genc.omnichannel.order.model.Order;
import com.genc.omnichannel.order.model.OrderItems;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {
    public Order toEntity(PlaceOrderRequest dto) {
        Order order = new Order();
        order.setOrderChannel(dto.getOrderChannel());
        return order;
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(order.getOrderId());
        orderResponse.setCustomerId(order.getCustomer() != null ? order.getCustomer().getCustomerId() : null);
        orderResponse.setOrderChannel(order.getOrderChannel());
        orderResponse.setTotalAmount(order.getTotalAmount());
        orderResponse.setOrderDate(order.getOrderDate());
        orderResponse.setOrderStatus(order.getOrderStatus());
        if (order.getItems() != null) {
            List<OrderItemResponse> itemResponses = order.getItems().stream()
                    .map(OrderMapper::toItemResponse)
                    .toList();
            orderResponse.setItems(itemResponses);
        }
        return orderResponse;
    }

    private static OrderItemResponse toItemResponse(OrderItems item) {
        BigDecimal unitPrice = item.getUnitPrice();
        Integer quantity = item.getQuantity();
        BigDecimal lineTotal = (unitPrice != null && quantity != null)
                ? unitPrice.multiply(BigDecimal.valueOf(quantity))
                : BigDecimal.ZERO;
        return new OrderItemResponse(
                item.getProduct() != null ? item.getProduct().getProductId() : null,
                item.getProduct() != null ? item.getProduct().getProductName() : null,
                quantity,
                unitPrice,
                lineTotal);
    }
}
