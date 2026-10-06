package com.example.coffee_order_system.domain.menu.dto;

import java.time.Instant;
import java.util.List;

public record PopularMenuListResponse(
        Instant periodStart,
        Instant periodEnd,
        List<PopularMenuResponse> menus
) {
}