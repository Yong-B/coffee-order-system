package com.example.coffee_order_system.domain.menu.controller;

import com.example.coffee_order_system.domain.menu.dto.PopularMenuListResponse;
import com.example.coffee_order_system.domain.menu.service.PopularMenuService;
import com.example.coffee_order_system.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class PopularMenuController {

    private final PopularMenuService popularMenuService;

    @GetMapping("/popular")
    public ApiResponse<PopularMenuListResponse> getPopularMenus() {
        return ApiResponse.ok(popularMenuService.getPopularMenus());
    }
}