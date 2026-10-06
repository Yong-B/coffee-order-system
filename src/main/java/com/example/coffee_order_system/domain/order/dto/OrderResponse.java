package com.example.coffee_order_system.domain.order.dto;

import com.example.coffee_order_system.domain.order.entity.OrderStatus;
import com.example.coffee_order_system.domain.payment.entity.PaymentStatus;

import java.time.Instant;

public record OrderResponse(
        Long orderId,
        Long paymentId,
        Long memberId,
        Long menuId,
        Long paymentAmount,
        Long remainingPoints,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        Instant paidAt
) {
}