package com.example.coffee_order_system.domain.point.dto;

public record PointChargeResponse(
        Long memberId,
        Long chargedAmount,
        Long balance
) {
}