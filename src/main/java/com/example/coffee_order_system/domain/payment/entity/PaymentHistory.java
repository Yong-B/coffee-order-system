package com.example.coffee_order_system.domain.payment.entity;

import com.example.coffee_order_system.domain.payment.event.PaymentCompletedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "payment_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private Long paymentId;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "payment_amount", nullable = false)
    private Long paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public static PaymentHistory from(PaymentCompletedEvent event) {
        PaymentHistory history = new PaymentHistory();

        history.paymentId = event.paymentId();
        history.orderId = event.orderId();
        history.memberId = event.memberId();
        history.menuId = event.menuId();
        history.paymentAmount = event.paymentAmount();
        history.status = PaymentStatus.PAID;
        history.paidAt =
                LocalDateTime.ofInstant(event.paidAt(), ZoneOffset.UTC);

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        history.recordedAt = now.withNano(
                (now.getNano() / 1_000) * 1_000
        );

        return history;
    }
}