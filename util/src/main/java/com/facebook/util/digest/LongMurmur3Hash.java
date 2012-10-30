package com.facebook.util.digest;

/**
 * adapter for the "repeatable murmur3 hash", MurmurHash createRepeatableHasher()
 */
public class LongMurmur3Hash implements DigestFunction<Long> {
  private static final LongMurmur3Hash INSTANCE = new LongMurmur3Hash();

  private final MurmurHash hasher = MurmurHash.createRepeatableHasher();

  /**
   * optionally use a singleton instance as the hasher is stateless, and hence thread safe
   * @return
   */
  public static LongMurmur3Hash getInstance() {
    return INSTANCE;
  }

  @Override
  public long computeDigest(Long input) {
    return hasher.hash(input);
  }
}
