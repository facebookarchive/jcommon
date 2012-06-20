package com.facebook.stats;

import com.google.common.base.Preconditions;
import org.joda.time.Duration;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableDuration;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;

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

  /**
   * Takes the oldest counter and returns the fraction [0, 1] of it that
   * has extended outside the current time window of the composite counter.
   * <p/>
   * Assumes:
   * counter.getEnd() >= window.getStart()
   * counter.getStart() < window.getStart()
   *
   * @param oldestCounter
   * @return fraction [0, 1]
   */
  protected float getExpiredFraction(EventCounterIf<C> oldestCounter) {
    ReadableDateTime windowStart = getWindowStart();

    //counter.getEnd() >= window.getStart()
    checkArgument( !oldestCounter.getEnd().isBefore(windowStart),
      "counter should have end %s >= window start %s", oldestCounter.getEnd(), windowStart
    );

    ReadableDateTime counterStart = oldestCounter.getStart();

    //counter.getstart() < window.getStart()
    checkArgument(
      counterStart.isBefore(windowStart),
      String.format(
        "counter should have start %s <= window start %s", counterStart, windowStart
      )
    );

    //
    long expiredPortionMillis = windowStart.getMillis() - counterStart.getMillis();
    long lengthMillis = oldestCounter.getEnd().getMillis() - counterStart.getMillis();
    float expiredFraction = expiredPortionMillis / (float) lengthMillis;

    Preconditions.checkState(
      expiredFraction >= 0 && expiredFraction <= 1.0,
      String.format(
        "%s not in [0, 1]", expiredFraction
      )
    );

    return expiredFraction;
  }
}
