package com.genc.omnichannel.order.repository;


import com.genc.omnichannel.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Order findByOrderId(Long orderId);

    List<Order> findAllByCustomer_CustomerId(Long customerId);
}
