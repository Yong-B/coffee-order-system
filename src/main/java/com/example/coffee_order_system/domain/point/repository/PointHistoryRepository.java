package com.example.coffee_order_system.domain.point.repository;

import com.example.coffee_order_system.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository
        extends JpaRepository<PointHistory, Long> {
}