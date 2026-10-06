package com.example.coffee_order_system.domain.point.controller;

import com.example.coffee_order_system.domain.point.dto.PointChargeRequest;
import com.example.coffee_order_system.domain.point.dto.PointChargeResponse;
import com.example.coffee_order_system.domain.point.service.PointService;
import com.example.coffee_order_system.global.error.BusinessException;
import com.example.coffee_order_system.global.error.ErrorCode;
import com.example.coffee_order_system.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

    private final PointService pointService;

    @PostMapping("/charges")
    public ApiResponse<PointChargeResponse> charge(
            @Valid @RequestBody PointChargeRequest request,
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

        PointChargeResponse response =
                pointService.charge(memberId, request.amount());

        return ApiResponse.ok(response);
    }
}