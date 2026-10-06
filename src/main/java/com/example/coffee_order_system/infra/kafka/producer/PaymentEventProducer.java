package com.example.coffee_order_system.infra.kafka.producer;

import com.example.coffee_order_system.domain.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    public void send(PaymentCompletedEvent event) {
        String payload = jsonMapper.writeValueAsString(event);

        kafkaTemplate.send(
                "payment-completed",
                event.paymentId().toString(),
                payload
        ).whenComplete((result, error) -> {
            if (error != null) {
                log.error(
                        "Kafka 발행 실패: paymentId={}, orderId={}",
                        event.paymentId(),
                        event.orderId(),
                        error
                );
                return;
            }

            log.info(
                    "Kafka 발행 성공: paymentId={}",
                    event.paymentId()
            );
        });
    }
}