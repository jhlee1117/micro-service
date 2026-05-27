package com.common.exceptions.code;

public interface ErrorCode {
    String getKey();       // i18n 키값 (예: system.tenant.exists)
    String getMessage();   // 디버깅 및 기본 노출용 메시지
    int getStatus();       // HTTP 상태 코드
}
