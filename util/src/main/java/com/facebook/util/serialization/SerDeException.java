package com.facebook.util.serialization;

public class SerDeException extends Exception {
  public SerDeException() {
  }

  public SerDeException(String message) {
    super(message);
  }

  public SerDeException(String message, Throwable cause) {
    super(message, cause);
  }

  public SerDeException(Throwable cause) {
    super(cause);
  }

  public SerDeException(
    String message,
    Throwable cause,
    boolean enableSuppression,
    boolean writableStackTrace
  ) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
