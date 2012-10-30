package com.facebook.util.digest;

public class IntegerIdentityDigest implements DigestFunction<Integer> {
  @Override
  public long computeDigest(Integer input) {
    return input;
  }
}
