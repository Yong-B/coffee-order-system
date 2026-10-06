package com.example.coffee_order_system.domain.order.repository;

import com.example.coffee_order_system.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
