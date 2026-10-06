package com.example.coffee_order_system.domain.point.entity;

public enum PointHistoryType {

    OPENING, // 기존 시스템에서 이전한 시작 잔액
    CHARGE,  // 충전
    PAYMENT  // 주문 결제 차감
}
