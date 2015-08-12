package com.facebook.memory.data.structures;

/**
 * result of updating the a CachePolicy.
 * Indicates if an eviction has occurred (means the key has been removed from the policy system)
 */
public class CacheAction {
  private final CachePolicyKey cachePolicyKey;
  private final boolean shouldEvict;
  private final long tokenToEvict;

  public CacheAction(CachePolicyKey cachePolicyKey, boolean shouldEvict, long tokenToEvict) {
    this.cachePolicyKey = cachePolicyKey;
    this.shouldEvict = shouldEvict;
    this.tokenToEvict = tokenToEvict;
  }

  /**
   * @return the cache policy key
   */
  public CachePolicyKey getCachePolicyKey() {
    return cachePolicyKey;
  }

  /**
   * @return did the policy evict the given
   */
  public boolean isShouldEvict() {
    return shouldEvict;
  }

  public long getTokenToEvict() {
    return tokenToEvict;
  }
}
