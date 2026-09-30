package com.example.coffee_order_system.domain.menu.controller;

import com.example.coffee_order_system.domain.menu.dto.MenuListResponse;
import com.example.coffee_order_system.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public MenuListResponse getMenus() {
        return menuService.getMenus();
    }
}