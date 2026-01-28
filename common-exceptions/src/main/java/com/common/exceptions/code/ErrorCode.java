package com.common.exceptions.code;

public interface ErrorCode {
    String getKey();       // i18n 키값 (예: system.tenant.exists)
    int getStatus();
}
