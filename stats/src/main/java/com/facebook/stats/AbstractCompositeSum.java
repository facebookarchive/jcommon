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
