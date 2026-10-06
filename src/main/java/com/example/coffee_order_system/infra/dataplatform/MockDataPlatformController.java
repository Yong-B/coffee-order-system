package com.example.coffee_order_system.infra.dataplatform;

import com.example.coffee_order_system.domain.payment.event.PaymentCompletedEvent;
import com.example.coffee_order_system.global.error.BusinessException;
import com.example.coffee_order_system.global.error.ErrorCode;
import com.example.coffee_order_system.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Profile("local")
@RequestMapping("/mock/order-events")
public class MockDataPlatformController {

    @PostMapping
    public ApiResponse<Void> receive(
            @RequestBody PaymentCompletedEvent event
    ) {
        if (event.paymentId() == null
                || event.orderId() == null
                || event.memberId() == null
                || event.menuId() == null
                || event.paymentAmount() == null
                || event.paymentAmount() <= 0
                || event.paidAt() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        log.info(
                "수집 플랫폼 수신: paymentId={}, orderId={}, "
                        + "memberId={}, menuId={}, paymentAmount={}, paidAt={}",
                event.paymentId(),
                event.orderId(),
                event.memberId(),
                event.menuId(),
                event.paymentAmount(),
                event.paidAt()
        );

        return ApiResponse.ok();
    }
}