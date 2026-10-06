package com.example.coffee_order_system.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name("payment-completed")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new FixedBackOff(
                        3_000L,
                        FixedBackOff.UNLIMITED_ATTEMPTS
                )
        );

        handler.addRetryableExceptions(IllegalStateException.class);

        return handler;
    }
}