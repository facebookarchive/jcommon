package com.facebook.stats;

/**
 * Aggregation operation that maintains the associative property on multiple
 * combinations. An aggregation is associative if the order in which the
 * operations are performed does not matter (e.g. sum, max, min).
 */
public interface AssociativeAggregation {
  public long combine(long l1, long l2);
}
