package com.genc.omnichannel.order.controller;

import com.genc.omnichannel.order.dto.OrderResponse;
import com.genc.omnichannel.order.dto.PlaceOrderRequest;
import com.genc.omnichannel.order.exception.OrderNotFoundException;
import com.genc.omnichannel.order.mapper.OrderMapper;
import com.genc.omnichannel.order.model.Order;
import com.genc.omnichannel.order.model.OrderStatus;
import com.genc.omnichannel.order.service.OrderService;
import com.genc.omnichannel.productcatalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderViewController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final ProductService productService;

    public OrderViewController(OrderService orderService, OrderMapper orderMapper, ProductService productService) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.productService = productService;
    }

    @GetMapping("/history")
    public String showOrderHistory(Model model) {
        List<Order> orders = orderService.getAllOrders();
        List<OrderResponse> orderResponses = new ArrayList<>();
        orders.forEach(order ->
                orderResponses.add(orderMapper.toResponse(order))
        );
        model.addAttribute("orders", orderResponses);
        return "order-management/orders";
    }

    @GetMapping("/history/customer")
    public String showCustomerOrderHistory(@RequestParam Long customerId, Model model) {
        List<Order> orders = orderService.getOrderHistory(customerId);
        List<OrderResponse> orderResponses = new ArrayList<>();
        orders.forEach(order -> {
            orderResponses.add(orderMapper.toResponse(order));
        });
        model.addAttribute("orders", orderResponses);
        model.addAttribute("customerId", customerId);
        return "order-management/orders";
    }

    @GetMapping("/new")
    public String showPlaceOrderForm(Model model) {
        model.addAttribute("placeOrderRequest", new PlaceOrderRequest());
        model.addAttribute("products", productService.getAllProducts());
        return "order-management/place-order";
    }


    @PostMapping("")
    public String placeOrder(@Valid @ModelAttribute("placeOrderRequest") PlaceOrderRequest placeOrderRequest, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.getAllProducts());
            return "order-management/place-order";
        }
        try {
            orderService.placeOrder(placeOrderRequest);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("products", productService.getAllProducts());
            return "order-management/place-order";
        } catch (RuntimeException ex) {
            // e.g. simulated payment failure (zero-total order) — show a friendly message, not a 500.
            model.addAttribute("error", "Could not place the order: " + ex.getMessage());
            model.addAttribute("products", productService.getAllProducts());
            return "order-management/place-order";
        }
        return "redirect:/orders/history";
    }


    @PostMapping("{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId, RedirectAttributes ra) {
        try {
            orderService.cancelOrder(orderId);
            ra.addFlashAttribute("flash", "Order #" + orderId + " cancelled.");
        } catch (IllegalStateException | IllegalArgumentException | OrderNotFoundException ex) {
            ra.addFlashAttribute("flash", ex.getMessage());
        }
        return "redirect:/orders/history";
    }

    @PostMapping("/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus newStatus, RedirectAttributes ra) {
        try {
            int pointsAwarded = orderService.updateOrderStatus(orderId, newStatus);
            String msg = "Order #" + orderId + " status updated to " + newStatus + ".";
            if (newStatus == OrderStatus.DELIVERED && pointsAwarded > 0) {
                msg += " " + pointsAwarded + " loyalty points awarded to the customer.";
            }
            ra.addFlashAttribute("flash", msg);
        } catch (IllegalStateException | IllegalArgumentException | OrderNotFoundException ex) {
            ra.addFlashAttribute("flash", ex.getMessage());
        }
        return "redirect:/orders/history";
    }

    @GetMapping("/{orderId}/status")
    public String showUpdateStatusForm(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        List<OrderStatus> allowedStatuses = orderService.getAllowedNextStatuses(order.getOrderStatus());
        model.addAttribute("order", orderMapper.toResponse(order));
        model.addAttribute("allowedStatuses", allowedStatuses);
        model.addAttribute("canDeliver", allowedStatuses.contains(OrderStatus.DELIVERED));
        model.addAttribute("pointsOnDelivery", order.getTotalAmount() != null ? order.getTotalAmount().intValue() : 0);
        return "order-management/update-status";
    }

    @GetMapping("/{orderId}")
    public String showOrderDetail(@PathVariable Long orderId, Model model, RedirectAttributes ra) {
        try {
            Order order = orderService.getOrderById(orderId);
            model.addAttribute("order", orderMapper.toResponse(order));
            return "order-management/order-detail";
        } catch (OrderNotFoundException | IllegalArgumentException ex) {
            ra.addFlashAttribute("flash", ex.getMessage());
            return "redirect:/orders/history";
        }
    }

}
