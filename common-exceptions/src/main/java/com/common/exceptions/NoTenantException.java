package com.common.exceptions;

public class NoTenantException extends RuntimeException {
  public NoTenantException(String message) {
    super(message);
  }
}
