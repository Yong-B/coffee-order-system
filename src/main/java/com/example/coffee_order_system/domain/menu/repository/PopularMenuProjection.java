package com.example.coffee_order_system.domain.menu.repository;

public interface PopularMenuProjection {

    Long getMenuId();

    String getName();

    Long getPrice();

    Long getOrderCount();
}