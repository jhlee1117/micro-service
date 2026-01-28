package com.microservices.auth_service.utils;

import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.common.exceptions.BusinessException;
import com.common.exceptions.code.ErrorCode;
import com.common.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException be)
    {
        ErrorCode errorCode = be.getErrorCode();

        // 에러 응답 객체 생성
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getKey(), be.getArgs());

        return ResponseEntity
            .status(errorCode.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse);

    }

    // 시스템 예외나 알 수 없는 에러 처리 (기존 Exception catch 역할)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        logger.error("Unhandled Exception: ", e);

        ErrorResponse response = ErrorResponse.of(
            "system.common.internalServerError",
            Map.of("message", e.getMessage())
        );

        return ResponseEntity.internalServerError().body(response);
    }
}
