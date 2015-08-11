package com.facebook.memory;

public class FailedAllocationException extends MemoryManagementException {

  public FailedAllocationException() {
  }

  public FailedAllocationException(String message) {
    super(message);
  }

  public FailedAllocationException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedAllocationException(Throwable cause) {
    super(cause);
  }

  public FailedAllocationException(
    String message,
    Throwable cause,
    boolean enableSuppression,
    boolean writableStackTrace
  ) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
