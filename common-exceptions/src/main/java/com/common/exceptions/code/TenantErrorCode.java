package com.common.exceptions.code;

public enum TenantErrorCode implements ErrorCode {
    ALREADY_EXISTS_TENANT("system.tenantManagement.alreadyExistsTenant", 409),
    TENANT_NOT_FOUND("system.tenantManagement.tenantNotFound", 409);

    private final String key;
    private final int status;

    TenantErrorCode(String key, int status) {
        this.key = key;
        this.status = status;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public int getStatus() {
        return this.status;
    }
}
