package com.example.coffee_order_system.infra.kafka.consumer;

import com.example.coffee_order_system.domain.payment.event.PaymentCompletedEvent;
import com.example.coffee_order_system.domain.payment.service.PaymentHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class PaymentHistoryConsumer {

    private final JsonMapper jsonMapper;
    private final PaymentHistoryService paymentHistoryService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "payment-history-group"
    )
    public void consume(String payload) {
        PaymentCompletedEvent event;

        try {
            event = jsonMapper.readValue(
                    payload,
                    PaymentCompletedEvent.class
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "결제 이벤트 JSON 변환에 실패했습니다.",
                    e
            );
        }

        // 별도 서비스의 DB 트랜잭션이 커밋된 후 반환됩니다.
        paymentHistoryService.record(event);
    }
}