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

import java.util.Arrays;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableDuration;

/**
 * stats class that is useful for tracking the number of events that occur in a given time window.
 *
 * <p>1. Does trimming of events based on the window size 2. Allows for updates of this window's
 * events based on updates to component windows (useful for overlapping window stats)
 *
 * <p>Optimal for write-heavy counters
 */
public class CompositeSum extends AbstractCompositeSum<EventCounter> implements EventCounter {

  public CompositeSum(ReadableDuration maxLength, ReadableDuration maxChunkLength) {
    super(maxLength, maxChunkLength);
  }

  public CompositeSum(ReadableDuration maxLength) {
    super(maxLength);
  }

  @Override
  protected EventCounter nextCounter(ReadableDateTime start, ReadableDateTime end) {
    return new EventCounterImpl(start, end);
  }

  @Override
  public EventCounter merge(EventCounter counter) {
    // special case to handle merging of 2 composite counters
    if (counter instanceof CompositeSum) {
      return internalMerge(
          ((CompositeSum) counter).getEventCounters(),
          new CompositeSum(getMaxLength(), getMaxChunkLength()));
    } else {
      return internalMerge(
          Arrays.asList(counter), new CompositeSum(getMaxLength(), getMaxChunkLength()));
    }
  }
}
