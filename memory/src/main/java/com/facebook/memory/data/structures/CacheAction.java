package com.facebook.memory.data.structures;

/**
 * result of updating the a CachePolicy.
 * - Indicates if an eviction has occurred (means the key has been removed from the policy system)
 * - on adds, will return a new CachePolicyKey as well
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
   * @return on adds to the cache policy, this will be the assigned policy key. On other calls, it will return
   * the same key as passed in (see CachePolicy)
   */
  public CachePolicyKey getCachePolicyKey() {
    return cachePolicyKey;
  }

  /**
   * @return did the policy evict the tokenToEvict from its policy?
   */
  public boolean isShouldEvict() {
    return shouldEvict;
  }

  /**
   * token the cache may have evicted
   *
   * @return
   */
  public long getTokenToEvict() {
    return tokenToEvict;
  }
}
