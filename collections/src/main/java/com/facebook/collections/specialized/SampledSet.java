package com.facebook.collections.specialized;

import com.facebook.collections.SnapshotProvider;
import com.facebook.collections.Trackable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

/**
 * interface that provides adaptive sampling for set size estimation. The
 * underlying set will contain only sampled values, but methods are available
 * to get the scaled size as well as the size of the sample and the max
 * sample size that can be used.
 * 
 * 
 * 
 * @param <T> type of element in the set
 */

@ThreadSafe
public interface SampledSet<T> extends Trackable, Set<T>, SnapshotProvider<SampledSet<T>> {
  
  public boolean add(T element);
  public int getMaxSetSize();
  public int getScaledSize();
  public int getSize();
  public int getSampleRate();
  public Set<T> getEntries();

  /**
   * takes a snapshot of the underlying elements in a set.  Also atomically
   * downsamples the snapshot's set if the requested rate is higher than what 
   * is presently stored
   * 
   * @param rate - rate to sample at. Will only use this if it is higher than
   * the existing sample rate (ie, it can't up-sample)
   * @return
   */
  public SampledSetSnapshot<T> sampleAt(int rate);

  /**
   * produces a new sampled set. The set is sampled at the higher rate
   * of the two sets and uses the max set size of the LHS
   * 
   * @param sampledSet 
   * @return
   */
  public SampledSet<T> merge(SampledSet<T> sampledSet);

  /**
   * merges another sampledSet's values into our own. The same semantics
   * as merge() apply with respect to the sampleRate
   * 
   * @param sampledSet 
   * @return true iff the underlying set is changed
   */
  public boolean mergeInPlaceWith(SampledSet<T> sampledSet);
}
