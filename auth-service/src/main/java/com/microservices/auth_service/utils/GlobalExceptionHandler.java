package com.microservices.auth_service.utils;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.common.exceptions.BusinessException;
import com.common.exceptions.code.ErrorCode;
import com.common.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. 비즈니스 예외 (지정된 상태 코드 반환)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException be) {
        ErrorCode errorCode = be.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getKey(), errorCode.getMessage(), be.getArgs());
        return ResponseEntity
            .status(errorCode.getStatus())
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse);
    }

    // 2. 인증 실패 (401 Unauthorized) - BadCredentialsException 등
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        logger.warn("Authentication failed: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
            "auth.error.unauthorized",
            "인증에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.",
            Map.of("detail", e.getMessage())
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 3. 인가 실패 (403 Forbidden) - 권한 부족
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("Access denied: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
            "auth.error.forbidden",
            "해당 기능에 접근할 권한이 없습니다.",
            Map.of("detail", e.getMessage())
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 4. 입력값 검증 실패 (400 Bad Request) - @Valid 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        logger.warn("Validation failed: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorResponse response = ErrorResponse.of(
            "system.common.badRequest",
            "입력값이 올바르지 않습니다: " + errorMessage,
            Map.of("field", e.getBindingResult().getFieldError().getField())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 5. 요청한 URL이 없을 때 (404 Not Found)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        logger.warn("No handler found: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
            "system.common.notFound",
            "요청하신 주소 또는 리소스를 찾을 수 없습니다.",
            Map.of("url", e.getRequestURL(), "method", e.getHttpMethod())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 6. 지원하지 않는 HTTP 메서드 호출 (405 Method Not Allowed)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.warn("Method not supported: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
            "system.common.methodNotAllowed",
            "지원하지 않는 요청 메서드입니다.",
            Map.of("method", e.getMethod())
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // 7. 지원하지 않는 미디어 타입 (415 Unsupported Media Type)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        logger.warn("Media type not supported: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
            "system.common.unsupportedMediaType",
            "지원하지 않는 미디어 타입입니다.",
            Map.of("contentType", e.getContentType() != null ? e.getContentType().toString() : "unknown")
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    // 8. 잘못된 JSON 파싱 등 요청 바디 에러 (400 Bad Request)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.warn("Message not readable: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
            "system.common.badRequest.parseError",
            "요청 데이터의 형식이 올바르지 않습니다. (JSON 문법 오류 등)",
            Map.of("detail", "Malformed JSON request")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 9. 필수 파라미터 누락 (400 Bad Request)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(MissingServletRequestParameterException e) {
        logger.warn("Missing parameter: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(
            "system.common.badRequest.missingParam",
            "필수 파라미터가 누락되었습니다: " + e.getParameterName(),
            Map.of("parameterName", e.getParameterName(), "parameterType", e.getParameterType())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 10. 기타 시스템 예외 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        logger.error("Unhandled Exception: ", e);
        ErrorResponse response = ErrorResponse.of(
            "system.common.internalServerError",
            e.getMessage(), 
            Map.of("errorType", e.getClass().getSimpleName())
        );
        return ResponseEntity.internalServerError().body(response);
    }
}
