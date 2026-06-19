package com.common.exceptions.code;

public enum UserErrorCode implements ErrorCode {

    ALREADY_EXISTS_USER("system.userManagement.alreadyExistsUser", "이미 존재하는 사용자입니다.", 409),
    USER_NOT_FOUND("system.userManagement.userNotFound", "사용자를 찾을 수 없습니다.", 404),
    INVALID_SIGNUP_TOKEN("auth.oauth.invalidSignupToken", "Invalid signup token.", 401),
    EXPIRED_SIGNUP_TOKEN("auth.oauth.expiredSignupToken", "Signup token has expired.", 410);

    private final String key;
    private final String message;
    private final int status;

    UserErrorCode(String key, String message, int status) {
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
