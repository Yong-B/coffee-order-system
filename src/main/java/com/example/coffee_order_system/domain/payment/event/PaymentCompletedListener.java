package com.example.coffee_order_system.domain.payment.event;

import com.example.coffee_order_system.infra.dataplatform.DataPlatformClient;
import com.example.coffee_order_system.infra.kafka.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedListener {

    private final PaymentEventProducer paymentEventProducer;
    private final DataPlatformClient dataPlatformClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        try {
            paymentEventProducer.send(event);
        } catch (Exception e) {
            log.error(
                    "Kafka 발행 요청 실패: paymentId={}",
                    event.paymentId(),
                    e
            );
        }

        try {
            dataPlatformClient.send(event);
        } catch (Exception e) {
            log.error(
                    "수집 플랫폼 전송 실패: paymentId={}",
                    event.paymentId(),
                    e
            );
        }
    }
}