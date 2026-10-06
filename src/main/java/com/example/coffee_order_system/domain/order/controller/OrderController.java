package com.example.coffee_order_system.domain.order.controller;

import com.example.coffee_order_system.domain.order.dto.OrderRequest;
import com.example.coffee_order_system.domain.order.dto.OrderResponse;
import com.example.coffee_order_system.domain.order.service.OrderService;
import com.example.coffee_order_system.global.error.BusinessException;
import com.example.coffee_order_system.global.error.ErrorCode;
import com.example.coffee_order_system.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> order(
            @Valid @RequestBody OrderRequest request,
            HttpServletRequest httpRequest
    ) {
        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        Long memberId = (Long) session.getAttribute(LOGIN_MEMBER_ID);

        if (memberId == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        OrderResponse response = orderService.order(
                memberId,
                request.menuId()
        );

        return ApiResponse.ok(response);
    }
}