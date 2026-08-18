package com.common.exceptions.code;

public enum RoleErrorCode implements ErrorCode {
  ALREADY_EXISTS_ROLE("system.roleManagement.alreadyExistsRole", "이미 존재하는 역할입니다.", 409),
  ROLE_NOT_FOUND("system.roleManagement.roleNotFound", "역할을 찾을 수 없습니다.", 404),
  NOT_PERMMITTED_ROLE("system.roleManagement.notPermittedRole", "삭제할 수 있는 권한이 없습니다.", 409);

  private final String key;
  private final String message;
  private final int status;

  RoleErrorCode(String key, String message, int status) {
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
