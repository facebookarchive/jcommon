package com.facebook.memory;

public class MemoryManagementException extends Exception {

  public MemoryManagementException() {
  }

  public MemoryManagementException(String message) {
    super(message);
  }

  public MemoryManagementException(String message, Throwable cause) {
    super(message, cause);
  }

  public MemoryManagementException(Throwable cause) {
    super(cause);
  }

  public MemoryManagementException(
    String message,
    Throwable cause,
    boolean enableSuppression,
    boolean writableStackTrace
  ) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
