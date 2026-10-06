package com.example.coffee_order_system.domain.point.entity;

import com.example.coffee_order_system.domain.order.entity.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "point_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_account_id", nullable = false)
    private PointAccount pointAccount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointHistoryType type;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static PointHistory charge(
            PointAccount account,
            Long amount
    ) {
        return create(
                account,
                null,
                PointHistoryType.CHARGE,
                amount
        );
    }

    public static PointHistory payment(
            PointAccount account,
            Order order
    ) {
        return create(
                account,
                order,
                PointHistoryType.PAYMENT,
                order.getPaymentAmount()
        );
    }

    private static PointHistory create(
            PointAccount account,
            Order order,
            PointHistoryType type,
            Long amount
    ) {
        PointHistory history = new PointHistory();

        history.pointAccount = account;
        history.order = order;
        history.type = type;
        history.amount = amount;
        history.balanceAfter = account.getBalance();

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        history.createdAt = now.withNano(
                (now.getNano() / 1_000) * 1_000
        );

        return history;
    }
}