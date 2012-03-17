package com.facebook.collections;

/**
 * utility class that holds a host:port.  
 * static factory method handles creating from a stirng 
 */
public class HostPort {
  private final String host;
  private final int port;
  
  private volatile String toStringResult;

  public static HostPort fromString(String str) {
    String[] parts = str.split(":");

    if (parts.length != 2) {
      throw new IllegalArgumentException("invalid host:post string: " + str);
    }
    
    return new HostPort(parts[0], Integer.valueOf(parts[1]));
  }

  public HostPort(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final HostPort hostPort = (HostPort) o;

    if (port != hostPort.port) {
      return false;
    }
    
    if (host != null ? !host.equals(hostPort.host) : hostPort.host != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = host != null ? host.hashCode() : 0;
    
    result = 31 * result + port;
    
    return result;
  }

  @Override
  public String toString() {
    if (toStringResult == null) {
      toStringResult = host + ":" + port;
    }
    
    return toStringResult;
  }
}
