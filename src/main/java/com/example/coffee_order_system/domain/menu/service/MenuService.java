package com.example.coffee_order_system.domain.menu.service;

import com.example.coffee_order_system.domain.menu.dto.MenuListResponse;
import com.example.coffee_order_system.domain.menu.dto.MenuResponse;
import com.example.coffee_order_system.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuListResponse getMenus() {
        List<MenuResponse> menus = menuRepository.findAllByOrderByIdAsc()
                .stream()
                .map(MenuResponse::from)
                .toList();

        return new MenuListResponse(menus);
    }
}