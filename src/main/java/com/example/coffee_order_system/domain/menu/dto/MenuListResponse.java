package com.example.coffee_order_system.domain.menu.dto;

import java.util.List;

public record MenuListResponse(
        List<MenuResponse> menus
) {
}