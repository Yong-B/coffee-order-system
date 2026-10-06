package com.example.coffee_order_system.domain.payment.service;

import com.example.coffee_order_system.domain.order.entity.Order;
import com.example.coffee_order_system.domain.payment.entity.Payment;
import com.example.coffee_order_system.domain.payment.event.PaymentCompletedEvent;
import com.example.coffee_order_system.domain.payment.entity.PaymentHistory;
import com.example.coffee_order_system.domain.payment.entity.PaymentStatus;
import com.example.coffee_order_system.domain.payment.repository.PaymentHistoryRepository;
import com.example.coffee_order_system.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional
    public void record(PaymentCompletedEvent event) {
        if (event == null || event.paymentId() == null) {
            throw new IllegalStateException("유효하지 않은 결제 이벤트입니다.");
        }

        Payment payment =
                paymentRepository.findByIdForUpdate(event.paymentId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "결제 원본이 존재하지 않습니다."
                                )
                        );

        Order order = payment.getOrder();

        boolean matches =
                payment.getStatus() == PaymentStatus.PAID
                        && Objects.equals(order.getId(), event.orderId())
                        && Objects.equals(
                        order.getMember().getId(),
                        event.memberId()
                )
                        && Objects.equals(
                        order.getMenu().getId(),
                        event.menuId()
                )
                        && Objects.equals(
                        payment.getAmount(),
                        event.paymentAmount()
                )
                        && payment.getPaidAt() != null
                        && Objects.equals(
                        payment.getPaidAt().toInstant(ZoneOffset.UTC),
                        event.paidAt()
                );

        if (!matches) {
            throw new IllegalStateException(
                    "결제 이벤트가 결제 원본과 일치하지 않습니다."
            );
        }

        if (paymentHistoryRepository.existsByPaymentId(event.paymentId())) {
            return;
        }

        paymentHistoryRepository.save(
                PaymentHistory.from(event)
        );
    }
}