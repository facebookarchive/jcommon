package com.facebook.util.digest;

public class IntegerDigestFunction implements DigestFunction<Integer> {
  private final MurmurHash hasher = new MurmurHash(MurmurHash.JCOMMON_SEED);

  @Override
  public long computeDigest(Integer input) {
    return hasher.hash(input);
  }
}
