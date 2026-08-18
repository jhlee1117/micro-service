package com.common.exceptions.code;

public enum MenuErrorCode implements ErrorCode {
  ALREADY_EXISTS_MENU("system.menuManagement.alreadyExistsMenu", "이미 존재하는 메뉴입니다.", 409),
  MENU_NOT_FOUND("system.menuManagement.menuNotFound", "메뉴를 찾을 수 없습니다.", 404);

  private final String key;
  private final String message;
  private final int status;

  MenuErrorCode(String key, String message, int status) {
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
