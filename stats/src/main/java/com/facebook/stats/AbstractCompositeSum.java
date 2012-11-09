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

import org.joda.time.ReadableDuration;

import java.util.Iterator;

public abstract class AbstractCompositeSum<C extends EventCounterIf<C>>
  extends AbstractCompositeCounter<C> {

  protected AbstractCompositeSum(ReadableDuration maxLength, ReadableDuration maxChunkLength) {
    super(maxLength, maxChunkLength);
  }

  protected AbstractCompositeSum(ReadableDuration maxLength) {
    super(maxLength);
  }

  /**
   * Note: this is a snapshot of some moment, but component EventCounters
   * may change value while the sum is computed. The only guarantee is that
   * no new EventCounters are added during the call.
   *
   * @return sum of counter in the range
   */
  @Override
  public synchronized long getValue() {
    trimIfNeeded();

    long value = 0L;
    Iterator<C> iter = getEventCounters().iterator();
    boolean first = true;

    while (iter.hasNext()) {
      C counter = iter.next();
      value += counter.getValue();

      if (first) {
        // if there is at least one counter that is partially expired
        if (counter.getStart().isBefore(getWindowStart())) {
          // adjust for partial expiration
          value -= (long) (getExpiredFraction(counter) * counter.getValue());
        }
        first = false;
      }
    }

    return value;
  }
}
