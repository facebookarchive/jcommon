package com.facebook.collections.specialized;

/**
 * this class represents a snapshot of the elements in a sampled set along
 * with the sample-rate for those elements.  In this way, it's a snapshot of 
 * elements that can be used to calculate the scaled size at a moment
 * 
 * in other words, the elements in this set are consistent with the sample
 * rate
 * 
 */
public class SampledSetSnapshot<T> {
  private final int sampleRate;
  private final int maxSetSize;
  private final SnapshotableSet<T> elements;

  SampledSetSnapshot(
    int sampleRate, int maxSetSize, SnapshotableSet<T> elements
  ) {
    this.sampleRate = sampleRate;
    this.maxSetSize = maxSetSize;
    this.elements = elements;
  }

  public int getSampleRate() {
    return sampleRate;
  }

  public int getMaxSetSize() {
    return maxSetSize;
  }

  public SnapshotableSet<T> getElements() {
    return elements;
  } 
}
