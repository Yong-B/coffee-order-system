package com.example.coffee_order_system.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequest(

        @NotNull(message = "메뉴 ID는 필수입니다.")
        @Positive(message = "메뉴 ID는 양수여야 합니다.")
        Long menuId
) {
}
