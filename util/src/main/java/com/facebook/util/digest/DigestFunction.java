package com.facebook.util.digest;

public interface DigestFunction<T> {
  public long computeDigest(T input);
}
