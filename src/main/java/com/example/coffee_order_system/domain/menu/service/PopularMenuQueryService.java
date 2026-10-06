package com.example.coffee_order_system.domain.menu.service;

import com.example.coffee_order_system.domain.menu.dto.PopularMenuListResponse;
import com.example.coffee_order_system.domain.menu.dto.PopularMenuResponse;
import com.example.coffee_order_system.domain.menu.repository.PopularMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularMenuQueryService {

    private final PopularMenuRepository popularMenuRepository;

    @Transactional(readOnly = true)
    public PopularMenuListResponse getPopularMenus() {
        Instant periodEnd = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant periodStart = periodEnd.minus(7, ChronoUnit.DAYS);

        LocalDateTime from =
                LocalDateTime.ofInstant(periodStart, ZoneOffset.UTC);
        LocalDateTime to =
                LocalDateTime.ofInstant(periodEnd, ZoneOffset.UTC);

        List<PopularMenuResponse> menus =
                popularMenuRepository.findPopularMenus(from, to)
                        .stream()
                        .map(PopularMenuResponse::from)
                        .toList();

        return new PopularMenuListResponse(
                periodStart,
                periodEnd,
                menus
        );
    }
}