package com.facebook.memory;

public enum  Sizes {
  KB(1024),
  MB(1024 * 1024),
  GB(1024 * 1024 * 1024),
  TB(1024 * 1024 * 1024 * 1024L),
  PB(1024 * 1024 * 1024 * 1024L * 1024),
  ;


  private final long sizeBytes;

  Sizes(long sizeBytes) {
    this.sizeBytes = sizeBytes;
  }

  public long of(long count) {
    return sizeBytes * count;
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  public int ov(int count) {
    return (int) (sizeBytes * count);
  }
}
