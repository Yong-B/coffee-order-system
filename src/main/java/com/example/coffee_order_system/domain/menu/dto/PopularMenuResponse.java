package com.example.coffee_order_system.domain.menu.dto;

import com.example.coffee_order_system.domain.menu.repository.PopularMenuProjection;

public record PopularMenuResponse(
        Long menuId,
        String name,
        Long price,
        Long orderCount
) {

    public static PopularMenuResponse from(PopularMenuProjection projection) {
        return new PopularMenuResponse(
                projection.getMenuId(),
                projection.getName(),
                projection.getPrice(),
                projection.getOrderCount()
        );
    }
}