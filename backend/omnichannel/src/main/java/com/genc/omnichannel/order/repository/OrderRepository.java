package com.genc.omnichannel.order.repository;

import com.genc.omnichannel.order.model.Order;
import com.genc.omnichannel.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Order findByOrderId(Long orderId);

    List<Order> findAllByCustomer_CustomerId(Long customerId);

    /**
     * Returns true if the customer has at least one order that is NOT in the given status.
     * Used for WELCOME10 first-time customer check:
     *   existsByCustomer_CustomerIdAndOrderStatusNot(customerId, OrderStatus.CANCELLED)
     * → true means the customer has a real (non-cancelled) order history.
     */
    boolean existsByCustomer_CustomerIdAndOrderStatusNot(Long customerId, OrderStatus orderStatus);
}
