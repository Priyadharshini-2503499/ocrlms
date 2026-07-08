package com.genc.omnichannel.order.service;

import com.genc.omnichannel.loyalty.model.Customer;
import com.genc.omnichannel.loyalty.repository.CustomerRepository;
import com.genc.omnichannel.loyalty.service.CustomerService;
import com.genc.omnichannel.order.dto.OrderItemRequest;
import com.genc.omnichannel.order.dto.PlaceOrderRequest;
import com.genc.omnichannel.order.exception.OrderNotFoundException;
import com.genc.omnichannel.order.model.Order;
import com.genc.omnichannel.order.model.OrderItems;
import com.genc.omnichannel.order.model.OrderStatus;
import com.genc.omnichannel.order.repository.OrderRepository;
import com.genc.omnichannel.productcatalog.model.Product;
import com.genc.omnichannel.productcatalog.model.ProductStatus;
import com.genc.omnichannel.productcatalog.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        CustomerService customerService,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.customerService = customerService;
        this.productRepository = productRepository;
    }

    @Transactional
    public void placeOrder(PlaceOrderRequest request) {

        if (request == null || request.getCustomerId() == null || request.getOrderChannel() == null) {
            throw new IllegalArgumentException("Invalid Arguments null value/s passed");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item.");
        }

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + request.getCustomerId()));

        Order order = new Order();
        order.setOrderChannel(request.getOrderChannel());
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus(OrderStatus.PLACED);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItems> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null || itemRequest.getQuantity() == null
                    || itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each item must have a product and a positive quantity.");
            }
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found with ID: " + itemRequest.getProductId()));
            if (product.getProductStatus() != ProductStatus.ACTIVE) {
                throw new IllegalArgumentException(
                        "Product '" + product.getProductName() + "' is not available for ordering.");
            }

            BigDecimal unitPrice = product.getBasePrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);

            orderItems.add(new OrderItems(order, product, itemRequest.getQuantity(), unitPrice));
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);

        if (!processPayment(total)) {
            throw new RuntimeException("Payment failed");
        }
        orderRepository.save(order);
    }

    @Transactional
    public int updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        if (!isValidTransition(order.getOrderStatus(), newStatus)) {
            throw new IllegalStateException("Can't Set order status from " + order.getOrderStatus() + " to " + newStatus);
        }
        order.setOrderStatus(newStatus);
        orderRepository.save(order);

        if (newStatus == OrderStatus.DELIVERED) {
            return awardLoyaltyPoints(order);
        }
        return 0;
    }

    private int awardLoyaltyPoints(Order order) {
        Customer customer = order.getCustomer();
        if (customer == null || order.getTotalAmount() == null) {
            return 0;
        }
        int earnedPoints = order.getTotalAmount().intValue();
        if (earnedPoints <= 0) {
            return 0;
        }
        int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
        customer.setLoyaltyPoints(currentPoints + earnedPoints);
        customerService.updateLoyaltyTier(customer); // saves and updates tier
        return earnedPoints;
    }

    public List<Order> getOrderHistory(Long customerId) {
        if (customerId == null) throw new IllegalArgumentException("Customer Id can't be null");
        return orderRepository.findAllByCustomer_CustomerId(customerId);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Invalid Arguments null value passed");
        }
        updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private boolean processPayment(BigDecimal totalAmount) {
        return totalAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.PLACED && (next == OrderStatus.CANCELLED || next == OrderStatus.CONFIRMED)) {
            return true;
        } else if (current == OrderStatus.CONFIRMED && (next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED)) {
            return true;
        } else return current == OrderStatus.SHIPPED && next == OrderStatus.DELIVERED;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<OrderStatus> getAllowedNextStatuses(OrderStatus current) {
        List<OrderStatus> orderStatuses = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            if (isValidTransition(current, status)) {
                orderStatuses.add(status);
            }
        }
        return orderStatuses;
    }
}
