
package com.example.coffee_order_system.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(
            HttpStatus.BAD_REQUEST,
            "COMMON_001",
            "잘못된 입력값입니다."
    ),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON_002",
            "서버 내부 오류가 발생했습니다."
    ),
    RESOURCE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COMMON_003",
            "요청한 리소스를 찾을 수 없습니다."
    ),

    // Member
    MEMBER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MEMBER_001",
            "회원을 찾을 수 없습니다."
    ),
    LOGIN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "MEMBER_002",
            "로그인이 필요합니다."
    ),
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "MEMBER_003",
            "아이디 또는 비밀번호가 올바르지 않습니다."
    ),

    // Menu
    MENU_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MENU_001",
            "메뉴를 찾을 수 없습니다."
    ),

    // Point
    INVALID_CHARGE_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "POINT_001",
            "충전금액은 양수여야 합니다."
    ),
    INSUFFICIENT_POINTS(
            HttpStatus.CONFLICT,
            "POINT_002",
            "보유 포인트가 부족합니다."
    ),
    POINT_BALANCE_LIMIT_EXCEEDED(
            HttpStatus.CONFLICT,
            "POINT_003",
            "충전 가능한 포인트 범위를 초과했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}