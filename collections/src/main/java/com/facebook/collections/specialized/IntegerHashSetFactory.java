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

import com.facebook.collections.SetFactory;

/**
 * Long in order to be compatible with SampledUniqueCount
 */
public class IntegerHashSetFactory implements SetFactory<Long, SnapshotableSet<Long>> {
  public static final int DEFAULT_MAX_SIZE = 8000;
  public static final int DEFAULT_INITIAL_SIZE = 4;

  private final int initialCapacity;
  private final int maxCapacity;

  public IntegerHashSetFactory(int initialCapacity, int maxCapacity) {
    this.initialCapacity = initialCapacity;
    this.maxCapacity = maxCapacity;
  }

  public IntegerHashSetFactory(int maxCapacity) {
    this(DEFAULT_INITIAL_SIZE, maxCapacity);
  }

  public IntegerHashSetFactory() {
    this(DEFAULT_INITIAL_SIZE, DEFAULT_MAX_SIZE);
  }

  public static IntegerHashSetFactory withInitialSize(int initialSize) {
    return new IntegerHashSetFactory(initialSize, DEFAULT_MAX_SIZE);
  }

  public static IntegerHashSetFactory withMaxSize(int maxSize) {
    return new IntegerHashSetFactory(DEFAULT_INITIAL_SIZE, maxSize);
  }

  @Override
  public SnapshotableSet<Long> create() {
    // TODO: the 2 *  should be smaller, maybe 1.2, but using 2 to avoid current bug in
    // SampledSetImpl that seem to exceed the max
    return new IntegerHashSet(initialCapacity, 2 * maxCapacity);
  }
}
