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

import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.map.OpenLongObjectHashMap;
import com.facebook.collections.SetFactory;

public class ColtHashSetFactory implements SetFactory<Long, SnapshotableSet<Long>> {
  // defaults are very memory conservative
  private static final int DEFAULT_INITIAL_VALUE = 3;
  private static final double DEFAULT_MIN_LOAD_FACTOR = 0.7;
  private static final double DEFAULT_MAX_LOAD_FACTOR = 0.9;

  private final NumberType numberType;
  private final int initialValue;
  private final double minLoadFactor;
  private final double maxLoadFactor;

  public ColtHashSetFactory(
    NumberType numberType,
    int initialValue,
    double maxLoadFactor,
    double minLoadFactor
  ) {
    this.numberType = numberType;
    this.initialValue = initialValue;
    this.maxLoadFactor = maxLoadFactor;
    this.minLoadFactor = minLoadFactor;
  }

  public ColtHashSetFactory(NumberType numberType, int initialValue) {
    this(numberType, initialValue, DEFAULT_MIN_LOAD_FACTOR, DEFAULT_MAX_LOAD_FACTOR);
  }

  @Override
  public SnapshotableSet<Long> create() {
    if (numberType == NumberType.INTEGER) {
      return new ColtIntegerHashSet(
        new OpenIntObjectHashMap(initialValue, minLoadFactor, maxLoadFactor)
      );
    } else if (numberType == NumberType.LONG) {
      return new ColtLongHashSet(
        new OpenLongObjectHashMap(initialValue, minLoadFactor, maxLoadFactor)
      );
    } else {
      throw new IllegalStateException(String.format("unknown type %s", numberType));
    }
  }
}
