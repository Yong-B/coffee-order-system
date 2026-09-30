package com.example.coffee_order_system.domain.menu.dto;

import com.example.coffee_order_system.domain.menu.entity.Menu;

public record MenuResponse(
        Long menuId,
        String name,
        Long price
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice()
        );
    }
}