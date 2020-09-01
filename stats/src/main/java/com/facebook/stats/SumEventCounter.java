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
package com.facebook.stats;

import org.joda.time.ReadableDateTime;

/**
 * A bit of a hack for performance, but we can improve override the add() method call to get a bit
 * more performance.
 *
 * @deprecated see original simple implementation, {@link EventCounterImpl} which does the same
 *     thing, but avoids encapsulation violation
 */
@Deprecated
public class SumEventCounter extends AssociativeAggregationCounter {
  public static final AssociativeAggregation AGGREGATION =
      new AssociativeAggregation() {
        @Override
        public long combine(long l1, long l2) {
          return l1 + l2;
        }
      };

  public SumEventCounter(ReadableDateTime start, ReadableDateTime end, long initialValue) {
    super(start, end, AGGREGATION, initialValue);
  }

  public SumEventCounter(ReadableDateTime start, ReadableDateTime end) {
    this(start, end, 0L);
  }

  @Override
  public void add(long delta) {
    value.addAndGet(delta);
  }
}
