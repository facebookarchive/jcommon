package com.facebook.util.digest;

/**
 * used when the value given as input has already been hashed (or otherwise sufficient randomized)
 *
 * @param <T>
 */
public class LongIdentityDigestFunction implements DigestFunction<Long> {
  public static final LongIdentityDigestFunction INSTANCE = new LongIdentityDigestFunction();

  @Override
  public long computeDigest(Long input) {
    return input;
  }
}
