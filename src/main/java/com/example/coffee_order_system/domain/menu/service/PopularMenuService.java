package com.example.coffee_order_system.domain.menu.service;

import com.example.coffee_order_system.domain.menu.dto.PopularMenuListResponse;
import com.example.coffee_order_system.infra.redis.PopularMenuCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularMenuService {

    private final PopularMenuCache popularMenuCache;
    private final PopularMenuQueryService popularMenuQueryService;

    public PopularMenuListResponse getPopularMenus() {
        return popularMenuCache.get()
                .orElseGet(() -> {
                    PopularMenuListResponse response =
                            popularMenuQueryService.getPopularMenus();

                    popularMenuCache.put(response);

                    return response;
                });
    }
}