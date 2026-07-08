package com.genc.omnichannel.order.controller;

import com.genc.omnichannel.order.dto.OrderResponse;
import com.genc.omnichannel.order.dto.PlaceOrderRequest;
import com.genc.omnichannel.order.exception.OrderNotFoundException;
import com.genc.omnichannel.order.mapper.OrderMapper;
import com.genc.omnichannel.order.model.Order;
import com.genc.omnichannel.order.model.OrderStatus;
import com.genc.omnichannel.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderRestController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) Long customerId) {
        List<Order> orders;
        if (customerId != null) {
            orders = orderService.getOrderHistory(customerId);
        } else {
            orders = orderService.getAllOrders();
        }
        List<OrderResponse> responses = new ArrayList<>();
        orders.forEach(order -> responses.add(orderMapper.toResponse(order)));
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(orderMapper.toResponse(order));
        } catch (OrderNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        try {
            orderService.placeOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Order placed successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Could not place the order: " + ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body) {
        try {
            String newStatusStr = body.get("newStatus");
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr);
            orderService.updateOrderStatus(id, newStatus);
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(orderMapper.toResponse(order));
        } catch (IllegalArgumentException | IllegalStateException | OrderNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(Map.of("message", "Order #" + id + " cancelled."));
        } catch (IllegalStateException | IllegalArgumentException | OrderNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}

