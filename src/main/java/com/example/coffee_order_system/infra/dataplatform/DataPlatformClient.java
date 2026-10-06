package com.example.coffee_order_system.infra.dataplatform;

import com.example.coffee_order_system.domain.payment.event.PaymentCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class DataPlatformClient {

    private final JsonMapper jsonMapper;
    private final URI endpoint;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public DataPlatformClient(
            JsonMapper jsonMapper,
            @Value("${data-platform.url}") String url
    ) {
        this.jsonMapper = jsonMapper;
        this.endpoint = URI.create(url);
    }

    public void send(PaymentCompletedEvent event) {
        String body = jsonMapper.writeValueAsString(event);

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<Void> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "수집 플랫폼 오류: HTTP " + response.statusCode()
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "수집 플랫폼 전송이 중단되었습니다.",
                    e
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "수집 플랫폼 전송에 실패했습니다.",
                    e
            );
        }
    }
}