package com.common.response;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

  // 1. 프런트엔드 i18n 키값
  private final String errorCode;

  // 2. 디버깅 및 기본 노출용 메시지
  private final String message;

  // 3. 다국어 메시지에 채워넣을 동적 파라미터들
  private final Map<String, Object> args;

  // 4. (선택) 클라이언트 디버깅을 위한 요청 시간
  private final LocalDateTime timestamp;

  // 5. (선택) 필요 시 서버 로그 추적을 위한 트래킹 ID
  private final String traceId;

  public static ErrorResponse of(String errorCode, String message, Map<String, Object> args) {
    return ErrorResponse.builder()
        .errorCode(errorCode)
        .message(message)
        .args(args)
        .timestamp(LocalDateTime.now())
        .build();
  }
}
