package com.common.exceptions.code;

public enum TenantErrorCode implements ErrorCode {
    ALREADY_EXISTS_TENANT("system.tenantManagement.alreadyExistsTenant", "이미 존재하는 테넌트입니다.", 409),
    TENANT_NOT_FOUND("system.tenantManagement.tenantNotFound", "테넌트를 찾을 수 없습니다.", 404);

    private final String key;
    private final String message;
    private final int status;

    TenantErrorCode(String key, String message, int status) {
        this.key = key;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public int getStatus() {
        return this.status;
    }
}
