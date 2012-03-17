package com.facebook.stats;

import org.joda.time.ReadableDuration;

import java.util.Iterator;

public abstract class AbstractCompositeSum<C extends EventCounterIf<C>>
  extends AbstractCompositeCounter<C> {

  protected AbstractCompositeSum(
    ReadableDuration maxLength,
    ReadableDuration maxChunkLength
  ) {
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

    Iterator<C> iter = eventCounterIterator();
    boolean first = true;
    while (iter.hasNext()) {
      C counter = iter.next();
      value += counter.getValue();

      if (first) {
        // if there is at least one counter that is partially expired
        if (getWindowStart().isAfter(counter.getStart())) {
          // adjust for partial expiration
          value -= (long) (getExpiredFraction(counter) * counter.getValue());
        }
        first = false;
      }
    }

    return value;
  }

  /**
   * Takes the oldest counter and returns the fraction [0, 1] of it that
   * has extended outside the current time window of the composite counter.
   *
   * Assumes:
   *   counter.getEnd() >= window.getStart()
   *   counter.getStart() <= window.getStart()
   *
   * @param oldestCounter
   * @return fraction [0, 1]
   */
  protected float getExpiredFraction(EventCounterIf<C> oldestCounter) {
    long expiredPortionMillis =
      getWindowStart().getMillis() - oldestCounter.getStart().getMillis();

    long lengthMillis =
      oldestCounter.getEnd().getMillis() - oldestCounter.getStart().getMillis();

    return (expiredPortionMillis / (float) lengthMillis);
  }
}
