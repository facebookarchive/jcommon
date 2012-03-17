package com.facebook.zookeeper;

public interface Encodable {
  /**
   * Interface for classes that can provide byte array encodings
   * @return an array of bytes
   */
  public byte[] encode();
}
