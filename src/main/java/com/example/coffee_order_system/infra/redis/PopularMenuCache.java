package com.example.coffee_order_system.infra.redis;

import com.example.coffee_order_system.domain.menu.dto.PopularMenuListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
public class PopularMenuCache {

    private static final String CACHE_KEY = "coffee:menus:popular:7days";

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final Duration cacheTtl;

    public PopularMenuCache(
            StringRedisTemplate redisTemplate,
            JsonMapper jsonMapper,
            @Value("${ranking.cache-ttl:30s}") Duration cacheTtl
    ) {
        if (cacheTtl.isZero() || cacheTtl.isNegative()) {
            throw new IllegalArgumentException(
                    "인기 메뉴 캐시 유효기간은 0보다 커야 합니다."
            );
        }

        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
        this.cacheTtl = cacheTtl;
    }

    public Optional<PopularMenuListResponse> get() {
        try {
            String json = redisTemplate.opsForValue().get(CACHE_KEY);

            if (json == null) {
                return Optional.empty();
            }

            PopularMenuListResponse response =
                    jsonMapper.readValue(json, PopularMenuListResponse.class);

            if (response == null
                    || response.periodStart() == null
                    || response.periodEnd() == null
                    || response.menus() == null) {
                return Optional.empty();
            }

            Instant now = Instant.now();

            // 조회 기준 시각으로부터 TTL이 지난 결과는 사용하지 않습니다.
            if (response.periodEnd().isAfter(now)
                    || !now.isBefore(response.periodEnd().plus(cacheTtl))) {
                return Optional.empty();
            }

            return Optional.of(response);
        } catch (Exception e) {
            log.warn(
                    "인기 메뉴 캐시 조회 실패. MySQL에서 조회합니다: {}",
                    e.toString()
            );
            return Optional.empty();
        }
    }

    public void put(PopularMenuListResponse response) {
        try {
            String json = jsonMapper.writeValueAsString(response);

            // DB 조회에 걸린 시간도 캐시 유효기간에 포함합니다.
            Duration remainingTtl = Duration.between(
                    Instant.now(),
                    response.periodEnd().plus(cacheTtl)
            );

            if (remainingTtl.toMillis() <= 0) {
                return;
            }

            // 여러 인스턴스가 동시에 조회했을 때 기존 캐시를 덮어쓰지 않습니다.
            redisTemplate.opsForValue().setIfAbsent(
                    CACHE_KEY,
                    json,
                    remainingTtl
            );
        } catch (Exception e) {
            log.warn(
                    "인기 메뉴 캐시 저장 실패. DB 조회 결과를 반환합니다: {}",
                    e.toString()
            );
        }
    }
}