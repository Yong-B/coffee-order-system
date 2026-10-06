package com.example.coffee_order_system.domain.payment.entity;

import com.example.coffee_order_system.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public static Payment create(Order order) {
        Payment payment = new Payment();
        payment.order = order;
        payment.amount = order.getPaymentAmount();
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

    public void complete() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        this.paidAt = now.withNano(
                (now.getNano() / 1_000) * 1_000
        );
        this.status = PaymentStatus.PAID;
    }
}