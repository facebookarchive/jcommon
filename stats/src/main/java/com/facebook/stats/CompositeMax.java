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
import org.joda.time.ReadableDuration;

import java.util.Arrays;
import java.util.Iterator;

public class CompositeMax extends AbstractCompositeCounter<EventCounter>
  implements EventCounter {

  public CompositeMax(
    ReadableDuration maxLength, ReadableDuration maxChunkLength
  ) {
    super(maxLength, maxChunkLength);
  }

  public CompositeMax(ReadableDuration maxLength) {
    super(maxLength);
  }

  @Override
  public EventCounter merge(EventCounter counter) {
    if (counter instanceof CompositeMax) {
      return internalMerge(
        ((CompositeMax) counter).getEventCounters(),
        new CompositeMax(getMaxLength(), getMaxChunkLength())
      );
    } else {
      return internalMerge(
        Arrays.asList(counter),
        new CompositeMax(getMaxLength(), getMaxChunkLength())
      );
    }
  }

  @Override
  protected EventCounter nextCounter(
    ReadableDateTime start, ReadableDateTime end
  ) {
    return new MaxEventCounter(start, end);
  }

  @Override
  public synchronized long getValue() {
    trimIfNeeded();

    long max = Long.MIN_VALUE;

    for (EventCounter eventCounter : getEventCounters()) {
      max = Math.max(max, eventCounter.getValue());
    }

    return max;
  }
}
