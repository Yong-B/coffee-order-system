package com.example.coffee_order_system.domain.menu.repository;

import com.example.coffee_order_system.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PopularMenuRepository extends Repository<Menu, Long> {

    @Query(value = """
            SELECT
                m.id AS menuId,
                m.name AS name,
                m.price AS price,
                COUNT(*) AS orderCount
            FROM orders o
            JOIN menus m ON m.id = o.menu_id
            WHERE o.status = 'PAID'
              AND o.paid_at >= :from
              AND o.paid_at < :to
            GROUP BY m.id, m.name, m.price
            ORDER BY COUNT(*) DESC, m.id ASC
            LIMIT 3
            """, nativeQuery = true)
    List<PopularMenuProjection> findPopularMenus(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}