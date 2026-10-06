package com.example.coffee_order_system.domain.order.entity;

import com.example.coffee_order_system.domain.member.entity.Member;
import com.example.coffee_order_system.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "payment_amount", nullable = false)
    private Long paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public static Order create(
            Member member,
            Menu menu,
            Long paymentAmount
    ) {
        Order order = new Order();
        order.member = member;
        order.menu = menu;
        order.paymentAmount = paymentAmount;
        order.status = OrderStatus.PENDING;
        return order;
    }

    public void markPaid(LocalDateTime paidAt) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 대기 중인 주문이 아닙니다.");
        }

        this.status = OrderStatus.PAID;
        this.paidAt = paidAt;
    }
}