package com.example.coffee_order_system.domain.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PointChargeRequest(

        @NotNull(message = "충전금액은 필수입니다.")
        @Positive(message = "충전금액은 0보다 커야 합니다.")
        Long amount
) {
}