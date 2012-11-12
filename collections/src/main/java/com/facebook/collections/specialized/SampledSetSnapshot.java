/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
