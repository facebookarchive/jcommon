package com.facebook.config;

public class ConfigException extends RuntimeException {
  public ConfigException() {
  }

  public ConfigException(String message) {
    super(message);
  }

  public ConfigException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigException(Throwable cause) {
    super(cause);
  }
}
