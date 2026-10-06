package com.example.coffee_order_system.domain.payment.event;

import java.time.Instant;

public record PaymentCompletedEvent(
        Long paymentId,
        Long orderId,
        Long memberId,
        Long menuId,
        Long paymentAmount,
        Instant paidAt
) {
}