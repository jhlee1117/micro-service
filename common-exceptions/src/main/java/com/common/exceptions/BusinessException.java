package com.common.exceptions;

import java.util.Collections;
import java.util.Map;
import com.common.exceptions.code.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> args;

    // 1. 단순 에러 코드만 던질 때
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getKey());
        this.errorCode = errorCode;
        this.args = Collections.emptyMap();
    }

    // 2. 동적 파라미터(ID, 이름 등)를 함께 던질 때
    public BusinessException(ErrorCode errorCode, Map<String, Object> args) {
        super(errorCode.getKey());
        this.errorCode = errorCode;
        this.args = args;
    }
}
