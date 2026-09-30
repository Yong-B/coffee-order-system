package com.example.coffee_order_system.global.error;

import com.example.coffee_order_system.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException e
    ) {
        log.warn("BusinessException: {}", e.getMessage());

        ErrorCode code = e.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code, e.getMessage()));
    }

    /**
     * DTO 검증 실패 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("Validation Error: {}", errorMessage);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_INPUT_VALUE,
                        errorMessage
                ));
    }

    /**
     * 요청 본문 누락, JSON 문법 오류, JSON 값 타입 불일치
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        // 로그인 비밀번호 등이 로그에 남지 않도록 요청 본문은 출력하지 않습니다.
        log.warn("HttpMessageNotReadableException: 요청 본문 변환 실패");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "요청 본문이 없거나 형식이 올바르지 않습니다."
                ));
    }

    /**
     * URL 파라미터 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        log.warn(
                "MethodArgumentTypeMismatchException: parameter={}",
                e.getName()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "요청 파라미터의 형식이 올바르지 않습니다."
                ));
    }

    /**
     * 일반 인자 오류
     * 사용자 입력 오류로 단정하지 않고 내부 오류로 처리합니다.
     * 예상 가능한 업무 오류는 BusinessException을 사용합니다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        log.error("IllegalArgumentException 발생", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * 일반 상태 오류
     * 내부 상태 문제를 400으로 숨기지 않고 500으로 처리합니다.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException e
    ) {
        log.error("IllegalStateException 발생", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * 존재하지 않는 리소스
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            NoResourceFoundException e,
            HttpServletRequest request
    ) {
        log.warn("404 Not Found: {}", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * 기존 로그인 코드 등에서 사용하는 ResponseStatusException
     * 지정된 HTTP 상태를 유지합니다.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(
            ResponseStatusException e
    ) {
        if (e.getStatusCode().is5xxServerError()) {
            log.error("ResponseStatusException 서버 오류", e);

            return ResponseEntity
                    .status(e.getStatusCode())
                    .headers(e.getHeaders())
                    .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
        }

        log.warn(
                "ResponseStatusException: status={}",
                e.getStatusCode().value()
        );

        String message = e.getReason();

        if (message == null || message.isBlank()) {
            message = "요청을 처리할 수 없습니다.";
        }

        return ResponseEntity
                .status(e.getStatusCode())
                .headers(e.getHeaders())
                .body(ApiResponse.error(
                        "HTTP_" + e.getStatusCode().value(),
                        message
                ));
    }

    /**
     * 나머지 예외 처리
     * Spring HTTP 예외는 원래 상태와 헤더를 유지합니다.
     * 그 외 예상하지 못한 예외는 500으로 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {

        if (e instanceof ErrorResponse errorResponse) {
            var status = errorResponse.getStatusCode();

            if (status.is5xxServerError()) {
                log.error("Spring HTTP 서버 오류", e);

                return ResponseEntity
                        .status(status)
                        .headers(errorResponse.getHeaders())
                        .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
            }

            log.warn(
                    "Spring HTTP 오류: type={}, status={}",
                    e.getClass().getSimpleName(),
                    status.value()
            );

            String message = switch (status.value()) {
                case 400 -> "요청 형식이나 필수 요청값을 확인해 주세요.";
                case 401 -> "인증이 필요합니다.";
                case 403 -> "접근 권한이 없습니다.";
                case 404 -> "요청한 리소스를 찾을 수 없습니다.";
                case 405 -> "지원하지 않는 HTTP 메서드입니다.";
                case 406 -> "요청한 응답 형식을 제공할 수 없습니다.";
                case 413 -> "요청 데이터 크기가 허용 범위를 초과했습니다.";
                case 415 -> "지원하지 않는 Content-Type입니다.";
                default -> "요청을 처리할 수 없습니다.";
            };

            return ResponseEntity
                    .status(status)
                    .headers(errorResponse.getHeaders())
                    .body(ApiResponse.error(
                            "HTTP_" + status.value(),
                            message
                    ));
        }

        log.error("Unhandled Exception 발생", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}