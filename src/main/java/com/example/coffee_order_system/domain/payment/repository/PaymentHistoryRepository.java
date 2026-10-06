package com.example.coffee_order_system.domain.payment.repository;

import com.example.coffee_order_system.domain.payment.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository
        extends JpaRepository<PaymentHistory, Long> {

    boolean existsByPaymentId(Long paymentId);
}